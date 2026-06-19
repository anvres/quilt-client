package tech.quilt.client.modules.impl.render;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector2f;
import tech.quilt.Quilt;
import tech.quilt.base.events.impl.render.EventRender2D;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.client.modules.api.setting.impl.BooleanSetting;
import tech.quilt.client.modules.api.setting.impl.NumberSetting;
import tech.quilt.client.modules.impl.combat.Aura;
import tech.quilt.utility.render.display.base.CustomDrawContext;
import tech.quilt.utility.render.display.base.color.ColorRGBA;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ModuleAnnotation(
   name = "Arrows",
   category = Category.RENDER,
   description = "Стрелки к игрокам на экране"
)
public final class Arrows extends Module {
   public static final Arrows INSTANCE = new Arrows();
   private final NumberSetting radius = new NumberSetting("Радиус", 70F, 50F, 160F, 1F);
   private final BooleanSetting dynamic = new BooleanSetting("Динамические", true);
   private float animatedRadius = 70F;
   private final Map<UUID, Float> smoothedAngles = new HashMap<>();

   private Arrows() {
   }

   @EventTarget
   public void onRender(EventRender2D event) {
      var player = mc.player;
      if (player == null) return;

      float targetRadius = radius.getCurrent();
      if (dynamic.isEnabled() && player.isSprinting()) {
         targetRadius += 20F;
      }
      animatedRadius += (targetRadius - animatedRadius) * 0.1F;

      CustomDrawContext ctx = event.getContext();
      for (PlayerEntity other : mc.world.getPlayers()) {
         if (other.equals(player)) continue;

         drawArrow(ctx, event.getContext().getMatrices(), other, other.getX(), other.getZ());
      }
   }

   private void drawArrow(CustomDrawContext ctx, MatrixStack stack, PlayerEntity target, double x, double z) {
      var player = mc.player;
      var window = mc.getWindow();
      int width = window.getScaledWidth();
      int height = window.getScaledHeight();

      float centerX = width / 2F;
      float centerY = height / 2F;

      float desiredAngle = MathHelper.wrapDegrees(getRotationTo(new Vector2f((float) x, (float) z)) - player.getYaw());
      float angle = smoothAngle(target.getUuid(), desiredAngle);

      stack.push();
      stack.translate(centerX, centerY, 0.0F);
      stack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle));
      stack.translate(-centerX, -centerY, 0.0F);

      ColorRGBA color;
      if (Quilt.getInstance().getFriendManager().isFriend(target.getName().getString())) {
         color = new ColorRGBA(20, 255, 20, 255);
      } else if (Aura.INSTANCE.isEnabled() && Aura.INSTANCE.getTarget() == target) {
         color = new ColorRGBA(255, 50, 50, 255);
      } else {
         color = new ColorRGBA(255, 255, 255, 255);
      }

      ctx.drawTexture(
         Quilt.id("images/triangle.png"),
         centerX - 7, centerY - animatedRadius,
         20, 20, color
      );
      stack.pop();
   }

   private float smoothAngle(UUID id, float targetAngle) {
      float prev = smoothedAngles.getOrDefault(id, targetAngle);
      float delta = MathHelper.wrapDegrees(targetAngle - prev);
      float factor = 0.08F;
      float result = prev + delta * factor;
      smoothedAngles.put(id, result);
      return result;
   }

   private float getRotationTo(Vector2f vec) {
      var player = mc.player;
      if (player == null) return 0F;

      double dx = vec.x - player.getX();
      double dz = vec.y - player.getZ();

      return (float) -(Math.atan2(dx, dz) * (180.0 / Math.PI));
   }
}
