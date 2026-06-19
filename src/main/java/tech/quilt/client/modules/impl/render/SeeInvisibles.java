package tech.quilt.client.modules.impl.render;

import com.darkmagician6.eventapi.EventTarget;
import tech.quilt.base.events.impl.entity.EventEntityColor;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.client.modules.api.setting.impl.NumberSetting;
import tech.quilt.utility.render.display.base.color.ColorRGBA;

@ModuleAnnotation(
   name = "See Invisibles",
   category = Category.RENDER,
   description = "Показывает невидимые сущности с настраиваемой прозрачностью"
)
public final class SeeInvisibles extends Module {
   public static final SeeInvisibles INSTANCE = new SeeInvisibles();

   private final NumberSetting alpha = new NumberSetting("Alpha", 0.3f, 0.0f, 1.0f, 0.1f, "Прозрачность");

   @EventTarget
   public void onEntityColor(EventEntityColor e) {
      int alphaValue = (int) (alpha.getCurrent() * 255);
      ColorRGBA color = new ColorRGBA(e.getColor()).withAlpha(alphaValue);
      e.setColor(color.getRGB());
      e.cancel();
   }
}
