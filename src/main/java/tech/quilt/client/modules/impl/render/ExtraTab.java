package tech.quilt.client.modules.impl.render;

import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;

@ModuleAnnotation(
   name = "ExtraTab",
   category = Category.RENDER,
   description = "Увеличивает количество игроков в табе"
)
public final class ExtraTab extends Module {
   public static final ExtraTab INSTANCE = new ExtraTab();

   private ExtraTab() {
   }
}
