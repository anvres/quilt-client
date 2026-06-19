package tech.quilt.client.modules.impl.render;

import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;

@ModuleAnnotation(
   name = "FullBright",
   category = Category.RENDER,
   description = "Максимальное освещение"
)
public class FullBright extends Module {
   public static final FullBright INSTANCE = new FullBright();
}
