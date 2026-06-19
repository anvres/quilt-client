package tech.quilt.client.modules.impl.player;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import tech.quilt.base.events.impl.other.EventTick;
import tech.quilt.base.events.impl.render.EventRender3D;
import tech.quilt.base.events.impl.server.EventPacket;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.client.modules.api.setting.impl.BooleanSetting;
import tech.quilt.client.modules.api.setting.impl.NumberSetting;
import tech.quilt.utility.math.Timer;
import tech.quilt.utility.render.display.base.color.ColorRGBA;
import tech.quilt.utility.render.level.Render3DUtil;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@ModuleAnnotation(
   name = "FakeLag",
   category = Category.PLAYER,
   description = "Симулирует лаг, задерживая пакеты"
)
public final class FakeLag extends Module {
   public static final FakeLag INSTANCE = new FakeLag();

   private final NumberSetting resetTime = new NumberSetting("Reset Time", 100.0f, 0.0f, 1000.0f, 10.0f, "Время до сброса (мс)");
   private final BooleanSetting renderBox = new BooleanSetting("Render", true);
   private final BooleanSetting resetOnKnockback = new BooleanSetting("Reset On Knockback", true);

   private Vec3d lastPos = Vec3d.ZERO;
   private final Queue<Packet<?>> packets = new ConcurrentLinkedQueue<>();
   private final Timer timer = new Timer();
   private boolean isCancel;

   @Override
   public void onEnable() {
      timer.reset();
      if (mc.player != null) {
         lastPos = mc.player.getPos();
      }
      super.onEnable();
   }

   @Override
   public void onDisable() {
      resetFakeLag();
      super.onDisable();
   }

   @EventTarget
   public void onTick(EventTick event) {
      if (cancelWork()) return;
      
      if (timer.finished((long) resetTime.getCurrent())) {
         resetFakeLag();
      }
   }

   @EventTarget
   public void onPacket(EventPacket event) {
      if (cancelWork()) return;
      
      if (event.isReceive()) {
         // Reset on explosion or knockback
         if (event.getPacket() instanceof ExplosionS2CPacket) {
            resetFakeLag();
         }
          
         if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket velocityPacket) {
            if (velocityPacket.getEntityId() == mc.player.getId() && resetOnKnockback.isEnabled()) {
               resetFakeLag();
            }
         }
      } else if (event.isSent()) {
         // Reset on certain player actions
         Packet<?> packet = event.getPacket();
         if (packet instanceof PlayerInteractEntityC2SPacket || 
             packet instanceof UpdateSelectedSlotC2SPacket || 
             packet instanceof HandSwingC2SPacket || 
             packet instanceof PlayerInteractBlockC2SPacket || 
             packet instanceof PlayerInteractItemC2SPacket || 
             packet instanceof ClickSlotC2SPacket) {
            resetFakeLag();
            return;
         }
          
         // Delay other packets
         if (!isCancel) {
            packets.add(packet);
            event.cancel();
         }
      }
   }

   @EventTarget
   public void onRender3D(EventRender3D event) {
      if (cancelWork() || !renderBox.isEnabled() || mc.player == null) return;
      
      Entity player = mc.player;
      Box box = new Box(
          lastPos.x, lastPos.y, lastPos.z,
          lastPos.x + player.getWidth(), 
          lastPos.y + player.getHeight(), 
          lastPos.z + player.getWidth()
      );
      
      Render3DUtil.drawBox(box, ColorRGBA.RED.withAlpha(200).getRGB(), 3.0f);
   }

   private boolean cancelWork() {
      return mc.isInSingleplayer();
   }

   private void resetFakeLag() {
      isCancel = true;
      while (!packets.isEmpty()) {
         mc.getNetworkHandler().sendPacket(packets.poll());
      }
      isCancel = false;
      timer.reset();
      if (mc.player != null) {
         lastPos = mc.player.getPos();
      }
   }
}
