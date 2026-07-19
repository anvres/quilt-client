package tech.quilt.client.modules.impl.misc;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import tech.quilt.base.events.impl.input.EventKey;
import tech.quilt.base.events.impl.other.EventTickMovement;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.client.modules.api.setting.impl.KeySetting;
import tech.quilt.utility.game.player.PlayerInventoryUtil;

@ModuleAnnotation(
   name = "FTHelper",
   category = Category.MISC,
   description = "Помощник для Funtime"
)
public final class FTHelper extends Module {
   public static final FTHelper INSTANCE = new FTHelper();

   // Настройки биндов
   private final KeySetting snowballKey = new KeySetting("Снежок Замароска");
   private final KeySetting godsAuraKey = new KeySetting("Божья Аура");
   private final KeySetting trapKey = new KeySetting("Трапка");
   private final KeySetting plastKey = new KeySetting("Пласт");
   private final KeySetting clearDustKey = new KeySetting("Явная пыль");
   private final KeySetting fireChargeKey = new KeySetting("Огненный заряд");
   private final KeySetting disorientationKey = new KeySetting("Дезориентация");

   private Item pendingItem = null;
   private int tickTimer = 0;
   private int itemSlot = -1;
   private int originalSlot = -1;
   private boolean f, b, l, r, j;

   private FTHelper() {
   }

   @Override
   public void onEnable() {
      super.onEnable();
      pendingItem = null;
      tickTimer = 0;
   }

   @Override
   public void onDisable() {
      super.onDisable();
      resetMovement();
      pendingItem = null;
      itemSlot = -1;
      tickTimer = 0;
   }

   @EventTarget
   public void onTick(EventTickMovement e) {
      if (mc.player == null || pendingItem == null) return;

      tickTimer++;

      if (tickTimer == 1) {
         f = mc.options.forwardKey.isPressed();
         b = mc.options.backKey.isPressed();
         l = mc.options.leftKey.isPressed();
         r = mc.options.rightKey.isPressed();
         j = mc.options.jumpKey.isPressed();

         mc.options.forwardKey.setPressed(false);
         mc.options.backKey.setPressed(false);
         mc.options.leftKey.setPressed(false);
         mc.options.rightKey.setPressed(false);
         mc.options.jumpKey.setPressed(false);

         mc.player.input.movementForward = 0;
         mc.player.input.movementSideways = 0;

         itemSlot = PlayerInventoryUtil.find(pendingItem, 0, 44);
         if (itemSlot == -1) {
            resetMovement();
            return;
         }
      }

      int selected = mc.player.getInventory().selectedSlot;
      int syncId = mc.player.currentScreenHandler.syncId;

      switch (tickTimer) {
         case 2 -> {
            if (itemSlot >= 9) {
               mc.interactionManager.clickSlot(syncId, itemSlot, selected, SlotActionType.SWAP, mc.player);
            } else {
               mc.player.getInventory().selectedSlot = itemSlot;
            }
         }
         case 3 -> {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            mc.player.swingHand(Hand.MAIN_HAND);
         }
         case 4 -> {
            if (itemSlot >= 9) {
               mc.interactionManager.clickSlot(syncId, itemSlot, selected, SlotActionType.SWAP, mc.player);
            } else {
               mc.player.getInventory().selectedSlot = originalSlot;
            }
         }
         case 5 -> resetMovement();
      }
   }

   @EventTarget
   public void onKey(EventKey e) {
      if (mc.player == null || pendingItem != null) return;
      if (mc.currentScreen != null) return;

      int key = e.getKeyCode();
      Item item = null;

      if (key == snowballKey.getKeyCode()) {
         item = Items.SNOWBALL;
      } else if (key == godsAuraKey.getKeyCode()) {
         item = Items.PHANTOM_MEMBRANE;
      } else if (key == trapKey.getKeyCode()) {
         item = Items.NETHERITE_SCRAP;
      } else if (key == plastKey.getKeyCode()) {
         item = Items.DRIED_KELP;
      } else if (key == clearDustKey.getKeyCode()) {
         item = Items.SUGAR;
      } else if (key == fireChargeKey.getKeyCode()) {
         item = Items.FIRE_CHARGE;
      } else if (key == disorientationKey.getKeyCode()) {
         item = Items.ENDER_EYE;
      }

      if (item != null && e.isKeyDown(key)) {
         int slot = PlayerInventoryUtil.find(item, 0, 44);
         if (slot == -1) return;
         originalSlot = mc.player.getInventory().selectedSlot;
         pendingItem = item;
         tickTimer = 0;
      }
   }

   private void resetMovement() {
      if (mc.player != null) {
         mc.options.forwardKey.setPressed(f);
         mc.options.backKey.setPressed(b);
         mc.options.leftKey.setPressed(l);
         mc.options.rightKey.setPressed(r);
         mc.options.jumpKey.setPressed(j);
      }
      pendingItem = null;
      itemSlot = -1;
      tickTimer = 0;
   }
}
