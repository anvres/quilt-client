package tech.quilt.client.modules.impl.misc;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import tech.quilt.base.events.impl.other.EventTick;
import tech.quilt.base.events.impl.player.EventUpdate;
import tech.quilt.base.events.impl.server.EventPacket;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.utility.game.player.PlayerInventoryUtil;

@ModuleAnnotation(
   name = "Joiner",
   category = Category.MISC,
   description = "Автоматический заход на дуэли через компас"
)
public final class Joiner extends Module {
   public static final Joiner INSTANCE = new Joiner();

   private boolean compassClick = false;
   private long last;
   private boolean restart;

   @Override
   public void onEnable() {
      compassClick = false;
      restart = false;
      super.onEnable();
   }

   @Override
   public void onDisable() {
      compassClick = false;
      restart = false;
      super.onDisable();
   }

   @EventTarget
   public void onUpdate(EventUpdate event) {
      if ((!compassClick || restart) && mc.currentScreen == null) {
         int compassSlot = PlayerInventoryUtil.find(Items.COMPASS, 0, 8);
         if (compassSlot == -1) return;

         // Select compass
         mc.player.getInventory().selectedSlot = compassSlot;
         
         // Use compass (right click)
         mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
         compassClick = true;
      }

      if (compassClick && mc.currentScreen instanceof GenericContainerScreen screen) {
         for (int i = 0; i < screen.getScreenHandler().slots.size(); i++) {
            ItemStack stack = screen.getScreenHandler().getSlot(i).getStack();
            if (stack != null && stack.getName() != null) {
               String displayName = stack.getName().getString();
               if (displayName.contains("Дуэли") || displayName.contains("Duels") || displayName.contains("duel")) {
                  // Swap compass to slot 4
                  if (i != 4) {
                     mc.interactionManager.clickSlot(screen.getScreenHandler().syncId, i, 4, SlotActionType.SWAP, mc.player);
                  }
                  compassClick = false;
                  restart = false;
                  break;
               }
            }
         }
      }
   }

   @EventTarget
   public void onPacket(EventPacket event) {
      if (!event.isReceive()) return;

      if (event.getPacket() instanceof GameMessageS2CPacket packet) {
         String message = packet.content().getString();
         if (message.contains("Вы уже подключены на этот сервер")
                 || message.contains("Сервер заполнен")
                 || message.contains("Вы были кикнуты с сервера 1duels")
                 || message.contains("Already connected")
                 || message.contains("Server is full")) {
            compassClick = false;
            restart = true;
            event.cancel();
         }
      }
   }
}
