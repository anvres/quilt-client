package tech.quilt.client.modules.impl.movement;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import tech.quilt.base.events.impl.player.EventMotion;
import tech.quilt.base.events.impl.player.EventOnTravelPost;
import tech.quilt.base.events.impl.player.EventUpdate;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.client.modules.api.setting.impl.ModeSetting;
import tech.quilt.client.modules.api.setting.impl.NumberSetting;
import tech.quilt.utility.game.player.MovingUtil;

@ModuleAnnotation(
   name = "Speed",
   category = Category.MOVEMENT,
   description = "Увеличивает скорость передвижения"
)
public final class Speed extends Module {
   public static final Speed INSTANCE = new Speed();

   private final ModeSetting mode = new ModeSetting("Mode", "Vanilla", "Grim");
   private final NumberSetting speed = new NumberSetting("Speed", 1.0F, 0.1F, 5.0F, 0.1F, () -> mode.is("Vanilla"));
   private final ModeSetting grimType = new ModeSetting("Grim mode", () -> mode.is("Grim"), "Collide", "Collide new");

   private Speed() {
   }

   @EventTarget
   public void onMotion(EventMotion event) {
      if (mc.player == null || mc.world == null) return;

      if (mode.is("Vanilla") && MovingUtil.hasPlayerMovement() && !mc.player.isGliding()) {
         MovingUtil.setVelocity(speed.getCurrent());
      }
   }

   @EventTarget
   public void onTravel(EventOnTravelPost event) {
      if (mc.player == null || mc.world == null) return;

      if (mode.is("Grim")) {
         if (grimType.is("Collide") || grimType.is("Collide new")) {
            boolean newMode = grimType.is("Collide new");
            int collisions = 0;

            for (Entity entity : mc.world.getEntities()) {
               if (entity instanceof LivingEntity living) {
                  if (living == mc.player) continue;
                  if (living instanceof ArmorStandEntity) continue;
                  if (hasCollisionWith(living, newMode ? 0f : 1f)) {
                     collisions++;
                  }
               }
            }

            if (collisions > 0) {
               double[] forward = MovingUtil.calculateDirection(0.08 * collisions);
               mc.player.addVelocity(forward[0], 0.0, forward[1]);
            }
         }
      }
   }

   private boolean hasCollisionWith(Entity entity, float expand) {
      return mc.player.getBoundingBox().expand((double)expand).intersects(entity.getBoundingBox());
   }
}
