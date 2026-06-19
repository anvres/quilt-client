package tech.quilt.client.modules.impl.combat.rotation;

import net.minecraft.client.option.Perspective;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import tech.quilt.client.modules.impl.combat.Aura;
import tech.quilt.client.modules.impl.movement.ElytraSample;
import tech.quilt.utility.component.RotationComponent;
import tech.quilt.utility.game.player.RaytracingUtil;
import tech.quilt.utility.game.player.rotation.Rotation;
import tech.quilt.utility.predict.PredictUtils;

public class LegendsGriefRotation extends RotationBase {
    private float acceleration = 0.0F;
    private boolean isBack = false;

    @Override
    public void update(Rotation angle, boolean elytraVisual) {
        update(null, angle, elytraVisual);
    }

    public void update(LivingEntity target, Rotation angle, boolean elytraVisual) {
        Box box = target != null ? target.getBoundingBox() : null;
        float predict = ElytraSample.INSTANCE.predict.getCurrent();

        Vec3d predictedOffset = (mc.player.isGliding() && target instanceof PlayerEntity && target.isGliding())
                ? PredictUtils.predict(target, target.getPos(), predict)
                : Vec3d.ZERO;

        if (mc.player.isGliding() && target instanceof PlayerEntity && target.isGliding()) {
            if (this.isBack) {
                if (this.acceleration >= -0.02F) {
                    float angleDiff = Math.abs(MathHelper.wrapDegrees(angle.getYaw() - this.lastYaw));
                    this.acceleration -= angleDiff > 80.0F ? 0.1F : 0.01F;
                }
                if (this.acceleration <= -0.02F) {
                    this.isBack = false;
                }
            } else {
                this.acceleration += 0.004F;
                if (this.acceleration >= 0.17F || (box != null &&
                        RaytracingUtil.rayTrace(mc.player.getRotationVector(), 1488.0D, box.offset(predictedOffset)))) {
                    this.isBack = true;
                }
            }
        } else {
            if (this.isBack) {
                if (this.acceleration >= -0.01F) {
                    float angleDiff = Math.abs(MathHelper.wrapDegrees(angle.getYaw() - this.lastYaw));
                    this.acceleration -= angleDiff > 80.0F ? 0.1F : 0.01F;
                }
                if (this.acceleration <= -0.01F) {
                    this.isBack = false;
                }
            } else {
                this.acceleration += 0.004F;
                if (this.acceleration >= 0.18F || (box != null &&
                        RaytracingUtil.rayTrace(mc.player.getRotationVector(), 999.0D, box.offset(predictedOffset).expand(-0.5D)))) {
                    this.isBack = true;
                }
            }
        }

        float deltaYaw = MathHelper.wrapDegrees(angle.getYaw() - this.lastYaw);
        float deltaPitch = angle.getPitch() - this.lastPitch;
        float smooth = Math.max(this.acceleration, 0.0F);

        float newYaw = this.lastYaw + deltaYaw * Math.min(Math.max(smooth, 0.0F), 1.0F);
        float newPitch = this.lastPitch + deltaPitch * Math.min(Math.max(smooth / 2.0F, 0.0F), 1.0F);

        float gcd = Rotation.gcd();
        newYaw -= (newYaw - this.lastYaw) % gcd;
        newPitch -= (newPitch - this.lastPitch) % gcd;

        Rotation smoothRot = new Rotation(newYaw, newPitch);

        float deltaYaw2 = MathHelper.wrapDegrees(mc.gameRenderer.getCamera().getYaw() - this.lastYaw);
        float deltaPitch2 = mc.gameRenderer.getCamera().getPitch() - this.lastPitch;

        if (mc.options.getPerspective() == Perspective.THIRD_PERSON_FRONT) {
            deltaYaw2 = MathHelper.wrapDegrees(mc.gameRenderer.getCamera().getYaw() - 180.0F - this.lastYaw);
            deltaPitch2 = -mc.gameRenderer.getCamera().getPitch() - this.lastPitch;
        }

        boolean viewOk = !(Math.abs(deltaYaw2) > 3.0F) && !(Math.abs(deltaPitch2) > 3.0F);
        float returnSpeed = viewOk ? 360.0F : 0.0F;

        RotationComponent.update(smoothRot, 360.0F, 360.0F, returnSpeed, returnSpeed, 0, 1, elytraVisual);
        this.lastYaw = smoothRot.getYaw();
        this.lastPitch = smoothRot.getPitch();
    }

    public void reset() {
        this.acceleration = 0.0F;
        this.isBack = false;
    }
}
