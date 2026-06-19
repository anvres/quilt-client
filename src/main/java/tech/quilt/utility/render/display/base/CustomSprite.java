package tech.quilt.utility.render.display.base;

import lombok.Generated;
import net.minecraft.util.Identifier;
import tech.quilt.Quilt;

public class CustomSprite {
   private final Identifier texture;

   public CustomSprite(String path) {
      if (path.contains(":")) {
         this.texture = Identifier.of(path);
      } else if (path.contains("/")) {
         this.texture = Quilt.id(path);
      } else {
         this.texture = Quilt.id("icons/category/" + path);
      }

   }

   @Generated
   public Identifier getTexture() {
      return this.texture;
   }
}
