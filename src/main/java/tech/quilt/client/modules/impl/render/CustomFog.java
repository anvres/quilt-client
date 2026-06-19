package tech.quilt.client.modules.impl.render;

import com.darkmagician6.eventapi.EventTarget;
import tech.quilt.Quilt;
import tech.quilt.base.events.impl.render.EventFog;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.client.modules.api.setting.impl.NumberSetting;

@ModuleAnnotation(
   name = "CustomFog",
   category = Category.RENDER,
   description = "Меняет туман"
)
public class CustomFog extends Module {
   public static final CustomFog INSTANCE = new CustomFog();
   public final NumberSetting distanceSetting = new NumberSetting("Дистанция тумана", 80.0F, 10.0F, 255.0F, 5.0F);

   @EventTarget
   public void onFog(EventFog e) {
      e.setDistance(this.distanceSetting.getCurrent());
      e.setColor(Quilt.getInstance().getThemeManager().getCurrentTheme().getColor().getRGB());
      e.setCancelled(true);
   }
}
