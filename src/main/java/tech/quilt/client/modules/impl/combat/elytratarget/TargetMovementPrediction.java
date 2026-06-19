package tech.quilt.client.modules.impl.combat.elytratarget;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import tech.quilt.client.modules.api.setting.impl.BooleanSetting;
import tech.quilt.client.modules.api.setting.impl.ModeSetting;
import tech.quilt.client.modules.api.setting.impl.NumberSetting;

import java.util.function.Supplier;

public class TargetMovementPrediction {

    public final BooleanSetting prediction;
    public final ModeSetting mode;
    public final ModeSetting.Value simple;
    public final ModeSetting.Value velocity;
    public final BooleanSetting glidingOnly;
    public final NumberSetting multiplier;

    public TargetMovementPrediction() {
        this(null);
    }

    public TargetMovementPrediction(Supplier<Boolean> visible) {
        this.prediction = visible != null
                ? new BooleanSetting("Prediction", true, visible)
                : new BooleanSetting("Prediction", true);
        this.mode = visible != null
                ? new ModeSetting("Predict Mode", visible)
                : new ModeSetting("Predict Mode", new String[0]);
        this.simple = new ModeSetting.Value(this.mode, "Simple").select();
        this.velocity = new ModeSetting.Value(this.mode, "Velocity");
        this.glidingOnly = visible != null
                ? new BooleanSetting("Gliding only", true, visible)
                : new BooleanSetting("Gliding only", true);
        this.multiplier = visible != null
                ? new NumberSetting("Multiplier", 1.8f, 0.5f, 6.0f, 0.1f, visible)
                : new NumberSetting("Multiplier", 1.8f, 0.5f, 6.0f, 0.1f);
    }

    public Vec3d predictPosition(LivingEntity target, Vec3d targetPosition) {
        if (!prediction.isEnabled() || getEntityBPS(target) < 13
                || (glidingOnly.isEnabled() && !target.isGliding())) {
            return targetPosition;
        }

        double mult = multiplier.getCurrent();
        if (velocity.isSelected()) {
            Vec3d simple = targetPosition.add(target.getVelocity().multiply(mult));
            return simple.subtract(0.0, 0.5 * 0.05 * mult * mult, 0.0);
        }
        // simple mode
        return targetPosition.add(target.getVelocity().multiply(mult));
    }

    private double getEntityBPS(LivingEntity entity) {
        double deltaX = entity.getX() - entity.prevX;
        double deltaZ = entity.getZ() - entity.prevZ;
        return Math.sqrt(deltaX * deltaX + deltaZ * deltaZ) * 20.0;
    }
}
