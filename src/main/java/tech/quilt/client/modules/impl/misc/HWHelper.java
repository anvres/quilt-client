package tech.quilt.client.modules.impl.misc;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import tech.quilt.base.events.impl.input.EventKey;
import tech.quilt.base.events.impl.player.EventUpdate;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.client.modules.api.setting.impl.BooleanSetting;
import tech.quilt.utility.math.Timer;

import java.util.LinkedHashMap;
import java.util.Map;

@ModuleAnnotation(
   name = "HWHelper",
   category = Category.MISC,
   description = "Быстрое взаимодействие с предметами на HollyWorld"
)
public final class HWHelper extends Module {
   public static final HWHelper INSTANCE = new HWHelper();

   public int trapkaKey = 0;
   public int trapkaBaxKey = 0;
   public int stanKey = 0;
   public int snowKey = 0;
   public int babaxKey = 0;

   private final BooleanSetting bypass = new BooleanSetting("Обход", true);
   private final BooleanSetting inventoryUse = new BooleanSetting("Использовать из инвентаря", true);

   private final Timer timer = new Timer();
   private boolean bypassActive = false;
   private boolean awaitingSwap = false;
   private int hotbarSlot = -1;
   private int invSlot = -1;

   private final Map<Integer, Item> binds = new LinkedHashMap<>();

   private HWHelper() {
      binds.put(0, Items.POPPED_CHORUS_FRUIT);
      binds.put(0, Items.PRISMARINE_SHARD);
      binds.put(0, Items.NETHER_STAR);
      binds.put(0, Items.SNOWBALL);
      binds.put(0, Items.FIRE_CHARGE);
   }

   @EventTarget
   public void onKey(EventKey event) {
      if (event.getAction() != 1) return;
      int pressedKey = event.getKeyCode();

      for (Map.Entry<Integer, Item> entry : binds.entrySet()) {
         if (pressedKey == entry.getKey()) {
            int[] slots = findSlots(entry.getValue());

            if (bypass.isEnabled()) {
               timer.reset();
               bypassActive = true;
               awaitingSwap = true;
               hotbarSlot = slots[0];
               invSlot = slots[1];
            } else {
               useItem(slots[0], slots[1], inventoryUse.isEnabled());
            }
            return;
         }
      }
   }

   @EventTarget
   public void onUpdate(EventUpdate event) {
      if (!bypassActive) return;

      setMovementKeys(false);

      if (awaitingSwap && timer.finished(90L)) {
         awaitingSwap = false;
         if (hotbarSlot != -1 || invSlot != -1) {
            useItem(hotbarSlot, invSlot, inventoryUse.isEnabled());
         }
      }

      if (timer.finished(150L)) {
         bypassActive = false;
         awaitingSwap = false;
         setMovementKeys(true);
      }
   }

   private int[] findSlots(Item item) {
      if (mc.player == null) return new int[]{-1, -1};

      int hotbarSlot = -1;
      int inventorySlot = -1;

      for (int i = 0; i < mc.player.getInventory().size(); i++) {
         ItemStack stack = mc.player.getInventory().getStack(i);
         if (stack.isEmpty() || stack.getItem() != item) continue;

         if (i < 9) hotbarSlot = i;
         else inventorySlot = i;

         if (hotbarSlot != -1 && inventorySlot != -1) break;
      }
      return new int[]{hotbarSlot, inventorySlot};
   }

   private void useItem(int hotbarSlot, int inventorySlot, boolean useFromInventory) {
      int currentItem = mc.player.getInventory().selectedSlot;
      if (hotbarSlot != -1) {
         mc.player.getInventory().selectedSlot = hotbarSlot;
         mc.interactionManager.interactItem(mc.player, net.minecraft.util.Hand.MAIN_HAND);
         mc.player.getInventory().selectedSlot = currentItem;
      } else if (useFromInventory && inventorySlot != -1) {
         mc.interactionManager.clickSlot(0, inventorySlot, currentItem, net.minecraft.screen.slot.SlotActionType.SWAP, mc.player);
         mc.interactionManager.interactItem(mc.player, net.minecraft.util.Hand.MAIN_HAND);
         mc.interactionManager.clickSlot(0, inventorySlot, currentItem, net.minecraft.screen.slot.SlotActionType.SWAP, mc.player);
      }
   }

   private void setMovementKeys(boolean restore) {
      KeyBinding[] keys = {
         mc.options.forwardKey,
         mc.options.backKey,
         mc.options.leftKey,
         mc.options.rightKey,
         mc.options.sprintKey
      };

      for (KeyBinding key : keys) {
         if (restore) {
            key.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), key.getDefaultKey().getCode()));
         } else {
            key.setPressed(false);
         }
      }
   }
}
