package tech.quilt.client.modules.impl.misc;

import com.darkmagician6.eventapi.EventTarget;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import tech.quilt.base.events.impl.player.EventUpdate;
import tech.quilt.base.events.impl.server.EventPacket;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.client.modules.api.setting.impl.ModeSetting;
import tech.quilt.client.modules.api.setting.impl.NumberSetting;
import tech.quilt.utility.math.Timer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.screen.slot.SlotActionType;

import java.util.List;

@ModuleAnnotation(
   name = "AutoDuel",
   category = Category.MISC,
   description = "Автоматически отправляет вызов на дуэль игрокам"
)
public final class AutoDuel extends Module {
   public static final AutoDuel INSTANCE = new AutoDuel();
   
   private final ModeSetting mode = new ModeSetting("Режим", "Ball", "Shield", "Spikes", "Netherite", "CheatParadise", "Bow", "Classic", "Totems", "NoDebuff");
   private final NumberSetting delay = new NumberSetting("Задержка", 1000, 500, 5000, 100);
   private final Timer timer = new Timer();
   private final Timer resetTimer = new Timer();
   private final Timer playerListUpdateTimer = new Timer();
   private final List<String> sent = Lists.newArrayList();
   private List<String> cachedPlayers = Lists.newArrayList();
   private int currentPlayerIndex = 0;

   private AutoDuel() {
      super();
   }

   @EventTarget
   public void onUpdate(EventUpdate event) {
      if (!isEnabled() || mc == null || mc.player == null || mc.world == null) return;
      
      // Обновляем список игроков раз в секунду (20 тиков), а не каждый тик
      if (playerListUpdateTimer.finished(1000L)) {
          updatePlayerList();
          playerListUpdateTimer.reset();
      }
      
      // Reset sent list periodically
      if (resetTimer.finished(800L * cachedPlayers.size())) {
          sent.clear();
          currentPlayerIndex = 0;
          resetTimer.reset();
      }

      if (!cachedPlayers.isEmpty()) {
          if (timer.finished(delay.getCurrent())) {
              if (currentPlayerIndex >= cachedPlayers.size()) {
                  currentPlayerIndex = 0;
              }

              String player = cachedPlayers.get(currentPlayerIndex);
              if (!sent.contains(player) && !player.equals(mc.getSession().getUsername())) {
                  mc.player.networkHandler.sendChatMessage("/duel " + player);
                  sent.add(player);
              }

              ++currentPlayerIndex;
              timer.reset();
          }

          // Handle GUI clicks for duel kit selection
          if (mc.currentScreen != null && mc.currentScreen.getTitle() != null && mc.player.currentScreenHandler != null) {
              String title = mc.currentScreen.getTitle().getString();
              
              if (title.contains("Выбор набора (1/1)")) {
                  if (timer.finished(150)) {
                      int slot = getSlotForMode(mode.get());
                      mc.interactionManager.clickSlot(
                          mc.player.currentScreenHandler.syncId,
                          slot,
                          0,
                          SlotActionType.QUICK_MOVE,
                          mc.player
                      );
                      timer.reset();
                  }
              } else if (title.contains("Настройка поединка") && timer.finished(150)) {
                  mc.interactionManager.clickSlot(
                      mc.player.currentScreenHandler.syncId,
                      0,
                      0,
                      SlotActionType.QUICK_MOVE,
                      mc.player
                  );
                  timer.reset();
              }
          }
      }
   }

   @EventTarget
   public void onPacket(EventPacket event) {
      if (!isEnabled() || mc == null || mc.player == null) return;
      
      if (event.isReceive() && event.getPacket() instanceof GameMessageS2CPacket) {
          GameMessageS2CPacket packet = (GameMessageS2CPacket) event.getPacket();
          String text = packet.content().getString().toLowerCase();
          if ((text.contains("начало") && text.contains("через") && text.contains("секунд!")) || text.isEmpty()) {
              toggle();
          }
      }
   }

   private void updatePlayerList() {
       List<String> players = Lists.newArrayList();
       if (mc.getNetworkHandler() != null && mc.getNetworkHandler().getPlayerList() != null) {
           for (PlayerListEntry playerEntry : mc.getNetworkHandler().getPlayerList()) {
               if (playerEntry.getProfile() != null) {
                   String name = playerEntry.getProfile().getName();
                   // Быстрая проверка без regex: длина 3-16 символов, только буквенно-цифровые
                   if (name != null && name.length() >= 3 && name.length() <= 16) {
                       boolean valid = true;
                       for (int i = 0; i < name.length(); i++) {
                           char c = name.charAt(i);
                           if (!Character.isLetterOrDigit(c) && c != '_') {
                               valid = false;
                               break;
                           }
                       }
                       if (valid) {
                           players.add(name);
                       }
                   }
               }
           }
       }
       cachedPlayers = players;
   }

   private int getSlotForMode(String mode) {
      switch (mode.toLowerCase()) {
          case "shield": return 0;
          case "spikes": return 1;
          case "bow": return 2;
          case "totems": return 3;
          case "nodebuff": return 4;
          case "ball": return 5;
          case "classic": return 6;
          case "cheatparadise": return 7;
          case "netherite": return 8;
          default: return 5; // Ball as default
      }
   }
}
