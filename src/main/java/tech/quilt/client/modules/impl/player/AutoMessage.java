package tech.quilt.client.modules.impl.player;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.LivingEntity;
import tech.quilt.base.events.impl.player.EventAttack;
import tech.quilt.base.events.impl.player.EventUpdate;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.client.modules.api.setting.impl.ModeSetting;
import tech.quilt.client.modules.api.setting.impl.NumberSetting;
import tech.quilt.client.modules.impl.combat.Aura;
import tech.quilt.utility.math.Timer;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@ModuleAnnotation(
   name = "AutoMessage",
   category = Category.PLAYER,
   description = "Автоматическое сообщение в чат"
)
public final class AutoMessage extends Module {
   public static final AutoMessage INSTANCE = new AutoMessage();

   private final ModeSetting mode = new ModeSetting("Отправлять",
      "После убийства", "Во время таргета", "По задержке");
   private final NumberSetting delay = new NumberSetting("Задержка", 5000F, 0F, 35000F, 1000F);
   private final String message = "Привет %target%!";

   private final Timer delayTimer = new Timer();
   private LivingEntity lastTarget;
   private boolean waitingForDeath = false;

   private final Pattern pattern = Pattern.compile(".*");

   private AutoMessage() {
   }

   @EventTarget
   public void onAttack(EventAttack event) {
      if (event.getTarget() instanceof LivingEntity entity) {
         if (mode.is("После убийства")) {
            lastTarget = entity;
            waitingForDeath = true;
         }
      }
   }

   @EventTarget
   public void onUpdate(EventUpdate event) {
      if (mc.player == null || mc.world == null) return;

      if (mode.is("По задержке")) {
         if (delayTimer.finished((long) delay.getCurrent())) {
            sendMessage(replaceTarget(message, null));
            delayTimer.reset();
         }
         return;
      }

      if (mode.is("Во время таргета")) {
         LivingEntity target = Aura.INSTANCE.getTarget();
         if (target != null && delayTimer.finished((long) delay.getCurrent())) {
            sendMessage(replaceTarget(message, target));
            delayTimer.reset();
         }
         return;
      }

      if (mode.is("После убийства") && waitingForDeath && lastTarget != null) {
         boolean dead = lastTarget.isDead() || lastTarget.getHealth() <= 0.0F;
         boolean unloaded = mc.world.getEntityById(lastTarget.getId()) == null;

         if (dead || unloaded) {
            sendMessage(replaceTarget(message, lastTarget));
            waitingForDeath = false;
            lastTarget = null;
            delayTimer.reset();
         }
      }
   }

   private void sendMessage(String msg) {
      if (msg == null || msg.trim().isEmpty()) return;
      mc.player.networkHandler.sendChatMessage(msg);
   }

   private String replaceTarget(String msg, LivingEntity target) {
      return msg.replace("%target%", target != null ? target.getName().getString() : "хряк");
   }

   private List<String> getOnlinePlayers() {
      return mc.player.networkHandler.getPlayerList().stream()
         .map(PlayerListEntry::getProfile)
         .map(profile -> profile.getName())
         .filter(profileName -> pattern.matcher(profileName).matches())
         .collect(Collectors.toList());
   }
}
