package tech.quilt.client.modules.impl.combat;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.entity.player.PlayerEntity;
import tech.quilt.base.events.impl.player.EventEntityHitBox;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.client.modules.api.setting.impl.NumberSetting;

@ModuleAnnotation(
   name = "HitBox",
   category = Category.COMBAT,
   description = "Увеличивает хитбокс игроков"
)
public final class HitBox extends Module {
   public static final HitBox INSTANCE = new HitBox();
   public final NumberSetting size = new NumberSetting("Размер", 0.4F, 0.1F, 5.5F, 0.1F);

   private HitBox() {
   }

   @EventTarget
   public void onHitBox(EventEntityHitBox event) {
      if (mc == null || mc.player == null || mc.world == null) return;
      
      // Only expand hitbox for other entities, not ourselves
      if (event.getEntity() == mc.player) return;
      
      // Only apply to players
      if (!(event.getEntity() instanceof PlayerEntity)) return;
      
      event.setSize(size.getCurrent());
   }
}
