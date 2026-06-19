package tech.quilt.client.modules.impl.player;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import tech.quilt.Quilt;
import tech.quilt.base.events.impl.player.EventUpdate;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.client.modules.api.setting.impl.ModeSetting;
import tech.quilt.client.modules.api.setting.impl.NumberSetting;

@ModuleAnnotation(
   name = "AutoLeave",
   category = Category.PLAYER,
   description = "Автоматически уходит с сервера при приближении игроков"
)
public final class AutoLeave extends Module {
   public static final AutoLeave INSTANCE = new AutoLeave();

   private final NumberSetting distance = new NumberSetting("Distance", 50.0f, 1.0f, 100.0f, 1.0f, "Дистанция");
   private final ModeSetting action = new ModeSetting("Action", new String[0]);
   private final ModeSetting.Value hub;
   private final ModeSetting.Value spawn;
   private final ModeSetting.Value home;

   public AutoLeave() {
      this.hub = new ModeSetting.Value(this.action, "Hub");
      this.spawn = new ModeSetting.Value(this.action, "Spawn").select();
      this.home = new ModeSetting.Value(this.action, "Home");
   }

   @EventTarget
   public void onUpdate(EventUpdate event) {
      for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
         if (mc.player == player) continue;
         if (Quilt.getInstance().getFriendManager().isFriend(player.getName().getString())) continue;

         if (player.getPos().distanceTo(mc.player.getPos()) <= distance.getCurrent()) {
            handleLeave();
            this.toggle();
            break;
         }
      }
   }

   private void handleLeave() {
      if (hub.isSelected()) {
         mc.player.networkHandler.sendChatCommand("hub");
      } else if (spawn.isSelected()) {
         mc.player.networkHandler.sendChatCommand("spawn");
      } else if (home.isSelected()) {
         mc.player.networkHandler.sendChatCommand("home home");
      }
   }
}
