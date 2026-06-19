package tech.quilt.client.modules.impl.movement;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.client.modules.api.setting.impl.BooleanSetting;
import tech.quilt.client.modules.api.setting.impl.ModeSetting;
import tech.quilt.client.modules.api.setting.impl.NumberSetting;
import tech.quilt.utility.predict.PredictUtils;

@ModuleAnnotation(
        name = "ElytraSample",
        category = Category.MOVEMENT,
        description = "Предсказание позиции цели на элитрах"
)
public final class ElytraSample extends Module {

    public static final ElytraSample INSTANCE = new ElytraSample();

    public final ModeSetting predictionType = new ModeSetting("Режим", "По тикам", "По смещению хитбокса", "По скорости");
    public final NumberSetting predict = new NumberSetting("Перегон (тики)", 2.0f, 1.0f, 8.0f, 0.1f,
            () -> predictionType.is("По тикам"));

    // По скорости settings
    public final BooleanSetting predictionEnabled = new BooleanSetting("Prediction", true,
            () -> predictionType.is("По скорости"));
    public final ModeSetting predictMode = new ModeSetting("Predict Mode",
            () -> predictionType.is("По скорости"), "Simple", "Velocity");
    public final BooleanSetting glidingOnly = new BooleanSetting("Gliding only", true,
            () -> predictionType.is("По скорости"));
    public final NumberSetting multiplier = new NumberSetting("Multiplier", 1.8f, 0.5f, 6.0f, 0.1f,
            () -> predictionType.is("По скорости"));

    private ElytraSample() {
    }

    /**
     * Вычисляет предсказанную позицию цели для элитра-перегона.
     *
     * @param target       цель
     * @param eyes         позиция глаз игрока
     * @param pingFactor   ping / 1000f
     * @param smoothedAimPoint текущая сглаженная точка прицела (для "По смещению хитбокса")
     * @param smoothedTargetVelocity сглаженная скорость цели (для "По смещению хитбокса")
     * @return предсказанная Vec3d позиция
     */
    public Vec3d sample(LivingEntity target, Vec3d eyes, float pingFactor,
                        Vec3d smoothedAimPoint, Vec3d smoothedTargetVelocity) {
        double distToTarget = eyes.distanceTo(target.getBoundingBox().getCenter());
        float basePrediction = distToTarget > 8.0D ? 8.0F : predict.getCurrent();
        float adjustedPrediction = basePrediction + pingFactor * 2.0F;

        Vec3d targetVel = target.getVelocity();
        Vec3d playerVel = mc.player.getVelocity();
        double relativeSpeed = playerVel.subtract(targetVel).horizontalLength();
        if (relativeSpeed > 1.5) adjustedPrediction += (float) (relativeSpeed * 0.3);

        if (predictionType.is("По тикам")) {
            Vec3d point = PredictUtils.predict(target, target.getPos(), adjustedPrediction);
            return point.add(targetVel.x * pingFactor * 3, targetVel.y * pingFactor, targetVel.z * pingFactor * 3);
        } else if (predictionType.is("По скорости")) {
            Vec3d pos = target.getPos();
            double bps = Math.sqrt(Math.pow(target.getX() - target.prevX, 2)
                    + Math.pow(target.getZ() - target.prevZ, 2)) * 20.0;
            if (predictionEnabled.isEnabled() && bps >= 13
                    && (!glidingOnly.isEnabled() || target.isGliding())) {
                double mult = multiplier.getCurrent();
                if (predictMode.is("Velocity")) {
                    Vec3d s = pos.add(target.getVelocity().multiply(mult));
                    return s.subtract(0.0, 0.5 * 0.05 * mult * mult, 0.0);
                } else {
                    return pos.add(target.getVelocity().multiply(mult));
                }
            }
            return pos;
        } else {
            // По смещению хитбокса — делегируем обратно в Aura
            return null;
        }
    }
}
