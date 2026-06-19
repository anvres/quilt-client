package tech.quilt.client.modules.impl.combat.rotation;

import net.minecraft.client.option.Perspective;
import net.minecraft.util.math.MathHelper;
import tech.quilt.utility.component.RotationComponent;
import tech.quilt.utility.game.player.RaytracingUtil;
import tech.quilt.utility.game.player.rotation.Rotation;

public class LonyJirRotation extends RotationBase {
    private float acceleration = 0.0F;
    private boolean isBack = false;

    @Override
    public void update(Rotation angle, boolean elytraVisual) {
        if (mc.player.isGliding()) {
            if (!this.isBack) {
                this.acceleration += 0.005F;
                if (this.acceleration >= 0.13F) {
                    this.isBack = true;
                }
            } else {
                if (this.acceleration >= -0.02F) {
                    this.acceleration -= 0.005F;
                }
                if (this.acceleration <= -0.02F) {
                    this.isBack = false;
                }
            }
        } else {
            if (!RaytracingUtil.rayTrace(mc.player.getRotationVector(), 1488.0D,
                    mc.player.getBoundingBox())) {
                this.acceleration += 0.0015F;
            } else if (this.acceleration > 0.0F) {
                this.acceleration -= 0.01F;
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
