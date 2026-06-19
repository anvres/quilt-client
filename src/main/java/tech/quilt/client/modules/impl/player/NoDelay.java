package tech.quilt.client.modules.impl.player;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import tech.quilt.base.events.impl.player.EventUpdate;
import tech.quilt.utility.mixin.accessors.MinecraftClientAccessor;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.client.modules.api.setting.impl.BooleanSetting;

@ModuleAnnotation(
   name = "NoDelay",
   category = Category.PLAYER,
   description = "Убирает задержку предметам"
)
public final class NoDelay extends Module {
   public static final NoDelay INSTANCE = new NoDelay();
   private final BooleanSetting jump = new BooleanSetting("Прыжок", true);
   private final BooleanSetting xp = new BooleanSetting("Пузырёк опыта", true);
   private final BooleanSetting crystal = new BooleanSetting("Кристаллы", true);
   private final BooleanSetting place = new BooleanSetting("ПКМ", false);

   private NoDelay() {
   }

   @EventTarget
   public void onUpdate(EventUpdate event) {
      if (mc.player == null) return;

      if (jump.isEnabled()) {
         mc.player.jumpingCooldown = 0;
      }

      if (check(mc.player.getMainHandStack().getItem())) {
         ((MinecraftClientAccessor) mc).setUseCooldown(0);
      }
   }

   private boolean check(Item item) {
      return (item instanceof BlockItem && place.isEnabled())
         || (item == Items.END_CRYSTAL && crystal.isEnabled())
         || (item == Items.EXPERIENCE_BOTTLE && xp.isEnabled());
   }
}
