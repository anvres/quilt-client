package tech.quilt.client.modules.impl.player;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import tech.quilt.base.events.impl.player.EventUpdate;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.client.modules.api.setting.impl.ModeSetting;
import tech.quilt.client.modules.api.setting.impl.NumberSetting;
import tech.quilt.utility.math.Timer;

import java.util.List;

@ModuleAnnotation(
   name = "ChestStealer",
   category = Category.PLAYER,
   description = "Автоматически забирает предметы из сундуков"
)
public final class ChestStealer extends Module {
   public static final ChestStealer INSTANCE = new ChestStealer();
   private final ModeSetting mode = new ModeSetting("Тип", "Обычный", "Умный");
   private final NumberSetting stealDelay = new NumberSetting("Задержка", 120F, 0F, 1000F, 1F);
   private final Timer timer = new Timer();

   private static final List<String> BLOCKED_TITLES = List.of(
      "Аукцион", "Warp", "Варпы", "Меню", "Выбор набора", "Кейсы", "Магазин"
   );

   private ChestStealer() {
   }

   @EventTarget
   public void onUpdate(EventUpdate event) {
      if (!(mc.currentScreen instanceof GenericContainerScreen container)) return;

      String title = container.getTitle().getString().toLowerCase();
      for (String blocked : BLOCKED_TITLES) {
         if (title.contains(blocked.toLowerCase())) return;
      }

      var handler = container.getScreenHandler();
      int chestSize = handler.getRows() * 9;
      boolean instant = stealDelay.getCurrent() == 0;

      for (int i = 0; i < chestSize; i++) {
         var stack = handler.getSlot(i).getStack();
         if (stack.isEmpty() || stack.getItem() == Items.AIR) continue;

         if (instant || timer.finished((long) stealDelay.getCurrent())) {
            mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
            if (!instant) {
               timer.reset();
               break;
            }
         }
      }
   }
}
