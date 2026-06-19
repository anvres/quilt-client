package tech.quilt.client.modules.impl.render;

import com.darkmagician6.eventapi.EventTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import ru.nexusguard.protection.annotations.Native;
import tech.quilt.Quilt;
import tech.quilt.base.animations.base.Animation;
import tech.quilt.base.animations.base.Easing;
import tech.quilt.base.events.impl.render.EventRender3D;
import tech.quilt.base.theme.Theme;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.client.modules.api.setting.impl.ModeSetting;
import tech.quilt.client.modules.impl.combat.Aura;
import tech.quilt.utility.render.display.base.color.ColorRGBA;
import tech.quilt.utility.render.display.shader.DrawUtil;

import java.awt.Color;

@ModuleAnnotation(
   name = "TargetESP",
   category = Category.RENDER,
   description = "Выделяет цель"
)
public class TargetESP extends Module {
   public static final TargetESP INSTANCE = new TargetESP();
   private final ModeSetting mode = new ModeSetting("Мод", "Маркер", "Маркер2", "Призраки", "Кружок");
   private final Animation animation;
   private final Animation animation2;
   private Entity lastTarget;
   private boolean textureLoaded;
   private float rotationAngle;
   private float rotationSpeed;
   private boolean isReversing;
   private float animationNurik;
   private long currentTime;
   private final float[] SCALE_CACHE;

   public TargetESP() {
      this.animation = new Animation(400L, Easing.CUBIC_OUT);
      this.animation2 = new Animation(250L, Easing.CUBIC_OUT);
      this.lastTarget = null;
      this.textureLoaded = false;
      this.rotationAngle = 0.0F;
      this.rotationSpeed = 0.0F;
      this.isReversing = false;
      this.animationNurik = 0.0F;
      this.SCALE_CACHE = new float[101];
      for (int i = 0; i <= 100; i++) {
         this.SCALE_CACHE[i] = Math.max(0.28f * (i / 100f), 0.2f);
      }
   }

   public void onEnable() {
      super.onEnable();
   }

   @EventTarget
   private void onRenderWorldLast(EventRender3D e) {
      if (this.mode.is("Призраки")) {
         this.drawSpiritsTrack(e);
         return;
      }

      if (this.mode.is("Кружок")) {
         this.drawCircle(e);
         return;
      }

      if (!this.textureLoaded) {
         MinecraftClient.getInstance().getTextureManager().registerTexture(Identifier.of("quilt", "hud/marker.png"), new ResourceTexture(Identifier.of("quilt", "hud/marker.png")));
         this.textureLoaded = true;
      }

      Vec3d camPos = mc.gameRenderer.getCamera().getPos();
      Entity target = Aura.INSTANCE.getTarget();
      if (target != null) {
         this.lastTarget = target;
         this.animation.update(true);
      } else {
         this.animation.update(false);
         if (this.animation.getValue() == 0.0F) {
            this.lastTarget = null;
         }
      }

      if (this.lastTarget != null) {
         double tickDelta = (double)e.getPartialTicks();
         MatrixStack matrices = e.getMatrix();
         double x = MathHelper.lerp(tickDelta, this.lastTarget.lastRenderX, this.lastTarget.getX());
         double y = MathHelper.lerp(tickDelta, this.lastTarget.lastRenderY, this.lastTarget.getY()) + (double)this.lastTarget.getHeight() / 2.0D;
         double z = MathHelper.lerp(tickDelta, this.lastTarget.lastRenderZ, this.lastTarget.getZ());
         matrices.push();
         matrices.translate(x - camPos.x, y - camPos.y, z - camPos.z);
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-mc.gameRenderer.getCamera().getYaw()));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(mc.gameRenderer.getCamera().getPitch()));
         float scale = 0.15F * this.animation.getValue();
         matrices.scale(-scale, -scale, scale);
         this.updateRotation();
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(this.rotationAngle));
         RenderSystem.disableDepthTest();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();

         Identifier textureId;
         if (this.mode.is("Маркер2")) {
            textureId = Identifier.of("quilt", "icons/marker2.png");
         } else {
            textureId = Identifier.of("quilt", "icons/marker.png");
         }

         AbstractTexture abstractTexture = MinecraftClient.getInstance().getTextureManager().getTexture(textureId);
         int texId = abstractTexture.getGlId();
         float alpha = this.animation.getValue();
         float size = 12.0F;
         Theme theme = Quilt.getInstance().getThemeManager().getCurrentTheme();
         ColorRGBA color = theme.getColor().withAlpha((int)(alpha * 255.0F));
         DrawUtil.drawTexture(matrices, textureId, 0.0F - size / 2.0F, 0.0F - size / 2.0F, size, size, color);
         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
         matrices.pop();
      }
   }

   @Native
   private void updateRotation() {
      if (!this.isReversing) {
         this.rotationSpeed += 0.01F;
         if ((double)this.rotationSpeed > 2.3D) {
            this.rotationSpeed = 2.3F;
            this.isReversing = true;
         }
      } else {
         this.rotationSpeed -= 0.01F;
         if ((double)this.rotationSpeed < -2.3D) {
            this.rotationSpeed = -2.3F;
            this.isReversing = false;
         }
      }

      this.rotationAngle += this.rotationSpeed;
      this.rotationAngle %= 360.0F;
   }

   public static double interpolate(double current, double old, double scale) {
      return old + (current - old) * scale;
   }

   private void drawSpiritsTrack(EventRender3D event3D) {
      Aura aura = Aura.INSTANCE;
      this.animation2.update(aura.getTarget() != null && aura.isEnabled());
      if ((double)this.animation2.getValue() != 0.0D) {
         MatrixStack e = event3D.getMatrix();
         if (aura.getTarget() != null) {
            if (this.lastTarget == null) {
               this.currentTime = System.currentTimeMillis();
            }

            this.lastTarget = aura.getTarget();
         }

         if (this.lastTarget != null) {
            this.animationNurik += (float)(5L * (System.currentTimeMillis() - this.currentTime)) / 600.0F;
            this.currentTime = System.currentTimeMillis();
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            RenderSystem.setShaderTexture(0, Quilt.id("icons/glow.png"));
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(770, 1, 0, 1);
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            double x = interpolate(this.lastTarget.getX(), this.lastTarget.lastRenderX, (double)event3D.getPartialTicks()) - mc.gameRenderer.getCamera().getPos().getX();
            double y = interpolate(this.lastTarget.getY(), this.lastTarget.lastRenderY, (double)event3D.getPartialTicks()) - mc.gameRenderer.getCamera().getPos().getY();
            double z = interpolate(this.lastTarget.getZ(), this.lastTarget.lastRenderZ, (double)event3D.getPartialTicks()) - mc.gameRenderer.getCamera().getPos().getZ();
            int n2 = 3;
            int n3 = 12;
            int n4 = 3 * n2;
            e.push();
            BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

            for(int i = 0; i < n4; i += n2) {
               for(int j = 0; j < n3; ++j) {
                  ColorRGBA color = Quilt.getInstance().getThemeManager().getCurrentTheme().getColor();
                  float f2 = this.animationNurik + (float)j * 0.1F;
                  float f3 = 0.8F;
                  float f4 = 0.5F;
                  int n5 = (int)Math.pow((double)i, 2.0D);
                  e.push();
                  e.translate(x + (double)(f3 * MathHelper.sin(f2 + (float)n5)), y + (double)f4 + (double)(0.3F * MathHelper.sin(this.animationNurik + (float)j * 0.2F)) + (double)(0.2F * (float)i), z + (double)(f3 * MathHelper.cos(f2 - (float)n5)));
                  e.scale(this.animation2.getValue() * (0.005F + (float)j / 2000.0F), this.animation2.getValue() * (0.005F + (float)j / 2000.0F), this.animation2.getValue() * (0.005F + (float)j / 2000.0F));
                  e.multiply(mc.gameRenderer.getCamera().getRotation());
                  int n7 = -25;
                  int n8 = 50;
                  buffer.vertex(e.peek().getPositionMatrix(), (float)n7, (float)(n7 + n8), 0.0F).texture(0.0F, 1.0F).color(color.withAlpha((int)(this.animation2.getValue() * 255.0F)).getRGB());
                  buffer.vertex(e.peek().getPositionMatrix(), (float)(n7 + n8), (float)(n7 + n8), 0.0F).texture(1.0F, 1.0F).color(color.withAlpha((int)(this.animation2.getValue() * 255.0F)).getRGB());
                  buffer.vertex(e.peek().getPositionMatrix(), (float)(n7 + n8), (float)n7, 0.0F).texture(1.0F, 0.0F).color(color.withAlpha((int)(this.animation2.getValue() * 255.0F)).getRGB());
                  buffer.vertex(e.peek().getPositionMatrix(), (float)n7, (float)n7, 0.0F).texture(0.0F, 0.0F).color(color.withAlpha((int)(this.animation2.getValue() * 255.0F)).getRGB());
                  e.pop();
               }
            }

            BufferRenderer.drawWithGlobalProgram(buffer.end());
            e.pop();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            RenderSystem.blendFunc(770, 771);
            RenderSystem.enableCull();
         }

      }
   }

   private void drawCircle(EventRender3D event3D) {
      Vec3d camPos = mc.gameRenderer.getCamera().getPos();
      Entity target = Aura.INSTANCE.getTarget();
      if (target != null) {
         this.lastTarget = target;
         this.animation.update(true);
      } else {
         this.animation.update(false);
         if (this.animation.getValue() == 0.0F) {
            this.lastTarget = null;
         }
      }

      if (this.lastTarget == null) return;

      float tickDelta = event3D.getPartialTicks();
      MatrixStack matrices = event3D.getMatrix();
      double x = MathHelper.lerp((double)tickDelta, this.lastTarget.lastRenderX, this.lastTarget.getX()) - camPos.x;
      double z = MathHelper.lerp((double)tickDelta, this.lastTarget.lastRenderZ, this.lastTarget.getZ()) - camPos.z;
      double y = MathHelper.lerp((double)tickDelta, this.lastTarget.lastRenderY, this.lastTarget.getY()) - camPos.y + Math.min(Math.sin((double)System.currentTimeMillis() / 400.0) + 0.95, (double)this.lastTarget.getHeight());

      RenderSystem.disableDepthTest();
      RenderSystem.disableCull();
      RenderSystem.enableBlend();
      RenderSystem.blendFuncSeparate(770, 1, 0, 1);
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

      Theme theme = Quilt.getInstance().getThemeManager().getCurrentTheme();
      ColorRGBA themeColor = theme.getColor();
      float r = (float)themeColor.getRed() / 255.0F;
      float g = (float)themeColor.getGreen() / 255.0F;
      float b = (float)themeColor.getBlue() / 255.0F;

      float alpha = this.animation.getValue();
      float radius = this.lastTarget.getWidth() * 0.8F;

      for (float i = 0; i <= (float)(Math.PI * 2) + (float)(Math.PI * 5.0 / 100.0); i += (float)(Math.PI * 5.0 / 100.0)) {
         double vecX = x + radius * Math.cos(i);
         double vecZ = z + radius * Math.sin(i);

         buffer.vertex(matrix, (float)vecX, (float)(y - Math.cos((double)System.currentTimeMillis() / 400.0) / 2.0), (float)vecZ).color(r, g, b, 0.01F * alpha);
         buffer.vertex(matrix, (float)vecX, (float)y, (float)vecZ).color(r, g, b, 1.0F * alpha);
      }

      BufferRenderer.drawWithGlobalProgram(buffer.end());
      RenderSystem.enableDepthTest();
      RenderSystem.disableBlend();
      RenderSystem.blendFunc(770, 771);
      RenderSystem.enableCull();
   }
}
