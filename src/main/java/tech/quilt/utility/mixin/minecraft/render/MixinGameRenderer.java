package tech.quilt.utility.mixin.minecraft.render;

import com.darkmagician6.eventapi.EventManager;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.profiler.Profiler;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import tech.quilt.base.events.impl.render.EventAspectRatio;
import tech.quilt.base.events.impl.render.EventFov;
import tech.quilt.base.events.impl.render.EventHudRender;
import tech.quilt.base.events.impl.render.EventRender3D;
import tech.quilt.base.events.impl.render.EventRenderScreen;
import tech.quilt.client.modules.impl.render.Interface;
import tech.quilt.utility.interfaces.IMinecraft;
import tech.quilt.utility.render.display.base.CustomDrawContext;
import tech.quilt.utility.render.display.base.UIContext;
import tech.quilt.utility.render.level.Render3DUtil;

@Mixin({GameRenderer.class})
public abstract class MixinGameRenderer {
   @Unique
   private static CustomDrawContext cachedDrawContext;
   @Unique
   private static Matrix4f orthoMatrix = new Matrix4f();
   @Unique
   private static double lastCustomScale = -1;
   @Shadow
   private float zoom;
   @Shadow
   private float zoomX;
   @Shadow
   private float zoomY;

   @Shadow
   public abstract float getFarPlaneDistance();

   @Inject(
      method = {"getBasicProjectionMatrix"},
      at = {@At("TAIL")},
      cancellable = true
   )
   public void getBasicProjectionMatrixHook(float fovDegrees, CallbackInfoReturnable<Matrix4f> cir) {
      EventAspectRatio eventAspectRatio = new EventAspectRatio();
      EventManager.call(eventAspectRatio);
      if (eventAspectRatio.isCancelled()) {
         Matrix4f matrix4f = new Matrix4f();
         if (this.zoom != 1.0F) {
            matrix4f.translate(this.zoomX, -this.zoomY, 0.0F);
            matrix4f.scale(this.zoom, this.zoom, 1.0F);
         }

         matrix4f.perspective(fovDegrees * 0.017453292F, eventAspectRatio.getRatio(), 0.05F, this.getFarPlaneDistance());
         cir.setReturnValue(matrix4f);
      }

   }

   @ModifyExpressionValue(
      method = {"getFov"},
      at = {@At(
   value = "INVOKE",
   target = "Ljava/lang/Integer;intValue()I",
   remap = false
)}
   )
   private int hookGetFov(int original) {
      EventFov event = new EventFov();
      EventManager.call(event);
      return event.isCancelled() ? event.getFov() : original;
   }

   @Inject(
      method = {"renderWorld"},
      at = {@At(
   value = "FIELD",
   target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z",
   opcode = 180,
   ordinal = 0
)}
   )
   public void hookWorldRender(RenderTickCounter tickCounter, CallbackInfo ci, @Local(ordinal = 2) Matrix4f matrix4f) {
      MatrixStack matrixStack = new MatrixStack();
      matrixStack.multiplyPositionMatrix(matrix4f);
      Render3DUtil.setLastProjMat(RenderSystem.getProjectionMatrix());
      Render3DUtil.setLastModMat(RenderSystem.getModelViewMatrix());
      Render3DUtil.setLastWorldSpaceMatrix(matrix4f);
      EventRender3D event = new EventRender3D(matrixStack, tickCounter.getTickDelta(false));
      EventManager.call(event);
      Render3DUtil.onEventRender3D(event.getMatrix());
   }

   @Inject(
      method = {"render"},
      at = {@At(
   value = "FIELD",
   target = "Lnet/minecraft/client/MinecraftClient;world:Lnet/minecraft/client/world/ClientWorld;",
   opcode = 180,
   ordinal = 2
)},
      locals = LocalCapture.CAPTURE_FAILHARD
   )
   private void renderScreenHook(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci, Profiler profiler, boolean bl, int i, int j, Window window, Matrix4f matrix4f, Matrix4fStack matrix4fStack, DrawContext drawContext) {
      EventManager.call(new EventRenderScreen(UIContext.of(drawContext, i, j, IMinecraft.mc.getRenderTickCounter().getTickDelta(false))));
   }

   @Inject(
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/gui/DrawContext;draw()V",
   opcode = 180,
   shift = Shift.AFTER,
   ordinal = 0
)},
      method = {"render"}
   )
   void renderHudHook(RenderTickCounter tickCounter, boolean tick, CallbackInfo callbackInfo) {
      this.triggerHudRenderEvent(tickCounter);
   }

   @Unique
   private void triggerHudRenderEvent(RenderTickCounter tickCounter) {
      if (cachedDrawContext == null) {
         cachedDrawContext = new CustomDrawContext(IMinecraft.mc.getBufferBuilders().getEntityVertexConsumers());
      }
      
      double customScale = (double)Interface.INSTANCE.getCustomScale();
      double saveScale = MinecraftClient.getInstance().getWindow().getScaleFactor();
      
      // Только обновляем scale если он изменился
      if (customScale != lastCustomScale) {
         lastCustomScale = customScale;
         this.setScaleFactorOutAllMods(customScale);
      }
      
      float width = (float)IMinecraft.mc.getWindow().getScaledWidth();
      float height = (float)IMinecraft.mc.getWindow().getScaledHeight();
      orthoMatrix.setOrtho(0.0F, width, height, 0.0F, 1000.0F, 21000.0F);
      RenderSystem.setProjectionMatrix(orthoMatrix, ProjectionType.ORTHOGRAPHIC);
      RenderSystem.disableDepthTest();

      try {
         EventManager.call(new EventHudRender(cachedDrawContext, tickCounter.getTickDelta(false)));
      } catch (Exception var6) {
         var6.printStackTrace();
      }

      cachedDrawContext.draw();
      RenderSystem.enableDepthTest();
      
      // Восстанавливаем оригинальный scale
      if (saveScale != lastCustomScale) {
         this.setScaleFactorOutAllMods(saveScale);
      }
      RenderSystem.setProjectionMatrix(orthoMatrix, ProjectionType.ORTHOGRAPHIC);
   }

   @Unique
   public void setScaleFactorOutAllMods(double scaleFactor) {
      if (IMinecraft.mc.getWindow().scaleFactor == scaleFactor) {
         return;
      }
      IMinecraft.mc.getWindow().scaleFactor = scaleFactor;
      int framebufferWidth = IMinecraft.mc.getWindow().framebufferWidth;
      int framebufferHeight = IMinecraft.mc.getWindow().framebufferHeight;
      IMinecraft.mc.getWindow().scaledWidth = (int)Math.ceil((double)framebufferWidth / scaleFactor);
      IMinecraft.mc.getWindow().scaledHeight = (int)Math.ceil((double)framebufferHeight / scaleFactor);
   }
}
