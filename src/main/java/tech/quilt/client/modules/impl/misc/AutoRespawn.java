package tech.quilt.client.modules.impl.misc;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.Screen;
import tech.quilt.base.events.impl.player.EventUpdate;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;

@ModuleAnnotation(
   name = "AutoRespawn",
   category = Category.MISC,
   description = "Автовозраждение после смерти"
)
public final class AutoRespawn extends Module {
   public static final AutoRespawn INSTANCE = new AutoRespawn();

   private AutoRespawn() {
   }

   @EventTarget
   public void onUpdate(EventUpdate event) {
      if (mc.player != null && mc.world != null) {
         if (mc.currentScreen instanceof DeathScreen && mc.player.deathTime > 5) {
            mc.player.requestRespawn();
            mc.setScreen((Screen)null);
         }

      }
   }
}
