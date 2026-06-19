package tech.quilt.client.modules.impl.combat.rotation;

import tech.quilt.utility.game.player.rotation.Rotation;
import tech.quilt.utility.interfaces.IMinecraft;

import java.security.SecureRandom;

public abstract class RotationBase implements IMinecraft {
    protected final SecureRandom rng = new SecureRandom();
    protected float lastYaw;
    protected float lastPitch;
    protected boolean noCircling;
    /** Множитель скорости ротаций. 1.0 = без изменений. Применяется к baseYawSpeed/basePitchSpeed в подклассах. */
    protected float speedMultiplier = 1.0F;

    public abstract void update(Rotation targetAngle, boolean elytraVisual);

    public float getYaw() { return lastYaw; }
    public float getPitch() { return lastPitch; }
    public void setYaw(float yaw) { this.lastYaw = yaw; }
    public void setPitch(float pitch) { this.lastPitch = pitch; }
    public void setNoCircling(boolean noCircling) { this.noCircling = noCircling; }
    public void setSpeedMultiplier(float multiplier) { this.speedMultiplier = multiplier; }
}
