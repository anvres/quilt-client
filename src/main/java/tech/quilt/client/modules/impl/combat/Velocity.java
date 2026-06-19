package tech.quilt.client.modules.impl.combat;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import tech.quilt.base.events.impl.server.EventPacket;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.client.modules.api.setting.impl.ModeSetting;

@ModuleAnnotation(
   name = "Velocity",
   category = Category.COMBAT,
   description = "Убирает отбрасывание"
)
public final class Velocity extends Module {
   public static final Velocity INSTANCE = new Velocity();
   public final ModeSetting mode = new ModeSetting("Тип", "Cancel");

   private Velocity() {
   }

   @EventTarget
   public void onPacket(EventPacket event) {
      if (event.isReceive() && event.getPacket() instanceof EntityVelocityUpdateS2CPacket packet) {
         if (packet.getEntityId() == mc.player.getId()) {
            if (mode.is("Cancel")) {
               event.cancel();
            }
         }
      }
   }
}
