package tech.quilt.utility.mixin.client.render;

import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tech.quilt.client.modules.impl.render.SwingAnimation;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

    @Shadow
    private void swingArm(float swingProgress, float equipProgress, MatrixStack matrices, int light, Arm arm) {
        // This is a shadow method that will be filled in by Mixin
    }

    @Redirect(
        method = {"renderFirstPersonItem"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/item/HeldItemRenderer;swingArm(FFLnet/minecraft/client/util/math/MatrixStack;ILnet/minecraft/util/Arm;)V"
        )
    )
    private void redirectSwingArm(HeldItemRenderer instance, float swingProgress, float equipProgress, MatrixStack matrices, int light, Arm arm) {
        SwingAnimation swingAnimation = SwingAnimation.INSTANCE;
        if (swingAnimation.isEnabled()) {
            swingAnimation.renderSwordAnimation(matrices, swingProgress, equipProgress, arm);
        } else {
            // Call the original method if SwingAnimation is disabled
            this.swingArm(swingProgress, equipProgress, matrices, light, arm);
        }
    }
}
