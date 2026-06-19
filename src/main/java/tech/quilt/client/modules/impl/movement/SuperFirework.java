package tech.quilt.client.modules.impl.movement;

import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.client.modules.api.setting.impl.BooleanSetting;
import tech.quilt.client.modules.api.setting.impl.ModeSetting;
import tech.quilt.client.modules.api.setting.impl.NumberSetting;

@ModuleAnnotation(
   name = "SuperFirework",
   category = Category.MOVEMENT,
   description = "Увеличивает буст от фейерверков"
)
public final class SuperFirework extends Module {
   public static final SuperFirework INSTANCE = new SuperFirework();
   public final ModeSetting mode = new ModeSetting("Мод", "BravoHvH", "ReallyWorld", "PulseHVH", "Custom");
   public final NumberSetting speed = new NumberSetting("Скорость", 1.70F, 1.50F, 8.00F, 0.01F);
   public final BooleanSetting nearBoost = new BooleanSetting("Ускорение если рядом игрок", false);

   public float speedXZ = 1.61F;
   public float speedY = 1.61F;

   public float diag1 = 5;
   public float diag2 = 5;
   public float diag3 = 5;
   public float diag4 = 5;
   public float diag5 = 5;
   public float diag6 = 5;
   public float diag7 = 5;
   public float diag8 = 5;
   public float diag9 = 5;
   public float diag10 = 5;

   public float speedD_1 = 1.5F;
   public float speedD_2 = 1.5F;
   public float speedD_3 = 1.5F;
   public float speedD_4 = 1.5F;
   public float speedD_5 = 1.5F;
   public float speedD_6 = 1.5F;
   public float speedD_7 = 1.5F;
   public float speedD_8 = 1.5F;
   public float speedD_9 = 1.5F;

   public float speedPitch = 1.5F;
   public float speedPitchY = 1.5F;

   public float speedNXZ = 1.5F;
   public float speedNY = 1.5F;

   private SuperFirework() {
   }

   public void onEnable() {
      super.onEnable();
      resetDefaults();
   }

   private void resetDefaults() {
      speedXZ = 1.61F;
      speedY = 1.61F;
      diag1 = 4; diag2 = 8; diag3 = 12; diag4 = 16; diag5 = 20;
      diag6 = 24; diag7 = 28; diag8 = 32; diag9 = 36; diag10 = 40;
      speedPitch = 2.5F;
      speedPitchY = 2.5F;
      speedD_1 = 2.2F; speedD_2 = 2.06F; speedD_3 = 1.98F;
      speedD_4 = 1.87F; speedD_5 = 1.8F; speedD_6 = 1.74F;
      speedD_7 = 1.7F; speedD_8 = 1.65F; speedD_9 = 1.63F;
      speedNXZ = 1.66F;
      speedNY = 1.66F;
   }
}
