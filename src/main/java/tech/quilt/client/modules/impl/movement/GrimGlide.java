package tech.quilt.client.modules.impl.movement;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import tech.quilt.base.events.impl.other.EventTick;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.client.modules.api.setting.impl.BooleanSetting;
import tech.quilt.client.modules.api.setting.impl.NumberSetting;
import tech.quilt.client.modules.impl.combat.Aura;

@ModuleAnnotation(
   name = "GrimGlide",
   category = Category.MOVEMENT,
   description = "Elytra boost для Grim/ReallyWorld с Aura-трекингом"
)
public final class GrimGlide extends Module {
   public static final GrimGlide INSTANCE = new GrimGlide();

   private final NumberSetting checkDelay = new NumberSetting("Check delay (ms)", 50.0F, 10.0F, 200.0F, 5.0F);
   private final NumberSetting forwardEven = new NumberSetting("Forward even", 0.085F, 0.01F, 0.5F, 0.005F);
   private final NumberSetting forwardOdd = new NumberSetting("Forward odd", 0.09F, 0.01F, 0.5F, 0.005F);
   private final BooleanSetting trackAura = new BooleanSetting("Track Aura target", true);
   private final NumberSetting minDot = new NumberSetting("Min dot product", 0.5F, 0.1F, 1.0F, 0.05F, () -> trackAura.isEnabled());

   private long lastCheckTime;

   @Override
   public void onEnable() {
      lastCheckTime = 0L;
      super.onEnable();
   }

   @Override
   public void onDisable() {
      lastCheckTime = 0L;
      super.onDisable();
   }

   @EventTarget
   public void onTick(EventTick event) {
      if (mc.player == null || mc.world == null || !mc.player.isGliding()) return;

      long now = System.currentTimeMillis();
      if (now - lastCheckTime < checkDelay.getCurrent()) return;
      lastCheckTime = now;

      if (trackAura.isEnabled() && shouldTrackTarget()) return;

      float yaw = mc.player.getYaw();
      double forward = (mc.player.age % 2 == 0) ? forwardEven.getCurrent() : forwardOdd.getCurrent();

      double rad = Math.toRadians(yaw);
      double dx = -Math.sin(rad) * forward;
      double dz =  Math.cos(rad) * forward;

      mc.player.setVelocity(dx, mc.player.getVelocity().y, dz);

      if (mc.player.age % 2 == 0) {
         mc.player.setPosition(
            mc.player.getX() + dx,
            mc.player.getY(),
            mc.player.getZ() + dz
         );
      }
   }

   private boolean shouldTrackTarget() {
      if (!Aura.INSTANCE.isEnabled()) return false;
      LivingEntity target = Aura.INSTANCE.getTarget();
      if (target == null || !target.isGliding()) return false;

      Vec3d vel = target.getVelocity();
      Vec3d velH = new Vec3d(vel.x, 0.0, vel.z);
      if (velH.lengthSquared() < 0.01) {
         velH = target.getRotationVec(1.0f);
         velH = new Vec3d(velH.x, 0.0, velH.z);
      }
      if (velH.lengthSquared() < 0.1) return false;
      velH = velH.normalize();

      Vec3d toTarget = new Vec3d(
         mc.player.getX() - target.getX(),
         0.0,
         mc.player.getZ() - target.getZ()
      );
      return toTarget.dotProduct(velH) > minDot.getCurrent();
   }
}
