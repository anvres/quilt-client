package tech.quilt.client.modules.impl.movement;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.entity.effect.StatusEffects;
import tech.quilt.base.events.impl.player.EventMotion;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.client.modules.api.setting.impl.ModeSetting;
import tech.quilt.utility.game.player.MovingUtil;

@ModuleAnnotation(
   name = "Strafe",
   category = Category.MOVEMENT,
   description = "Быстрое перемещение"
)
public final class Strafe extends Module {
   public static final Strafe INSTANCE = new Strafe();
   private final ModeSetting mode = new ModeSetting("Тип", "MetaHvH");

   private Strafe() {
   }

   @EventTarget
   public void onMotion(EventMotion event) {
      if (mc.player == null) return;

      if (mode.is("MetaHvH")) {
         if (!mc.player.isGliding() && (!mc.player.isTouchingWater() || !mc.player.isSwimming())) {
            if (MovingUtil.hasPlayerMovement()) {
               float motion = 0.19F;

               var speedEffect = mc.player.getStatusEffect(StatusEffects.SPEED);
               if (speedEffect != null) {
                  int amplifier = speedEffect.getAmplifier();
                  motion = switch (amplifier) {
                     case 0 -> 0.25F;
                     case 1 -> 0.37F;
                     case 2 -> 0.46F;
                     case 3 -> 0.7F;
                     default -> 0.75F + (amplifier - 3) * 0.05F;
                  };
               }

               if (mc.options.jumpKey.isPressed()) {
                  motion += 0.1F;
               }

               MovingUtil.setVelocity(motion);
            }
         }
      }
   }
}
