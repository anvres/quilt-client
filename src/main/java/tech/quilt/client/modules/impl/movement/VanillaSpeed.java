package tech.quilt.client.modules.impl.movement;

import com.darkmagician6.eventapi.EventTarget;
import tech.quilt.base.events.impl.player.EventMotion;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.client.modules.api.setting.impl.ModeSetting;
import tech.quilt.client.modules.api.setting.impl.NumberSetting;
import tech.quilt.utility.game.player.MovingUtil;

@ModuleAnnotation(
   name = "VanillaSpeed",
   category = Category.MOVEMENT,
   description = "Базовое ускорение"
)
public final class VanillaSpeed extends Module {
   public static final VanillaSpeed INSTANCE = new VanillaSpeed();
   private final ModeSetting mode = new ModeSetting("Режим", "Vanilla");
   private final NumberSetting speed = new NumberSetting("Скорость", 1.0F, 0.1F, 3.0F, 0.1F);

   private VanillaSpeed() {
   }

   @EventTarget
   public void onMotion(EventMotion event) {
      if (mc.player == null || mc.world == null) return;

      if (mode.is("Vanilla") && MovingUtil.hasPlayerMovement() && !mc.player.isGliding()) {
         MovingUtil.setVelocity(speed.getCurrent());
      }
   }
}
