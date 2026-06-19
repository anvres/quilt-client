package tech.quilt.client.modules.impl.misc;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.text.MutableText;
import tech.quilt.base.events.impl.other.EventTick;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.client.modules.api.setting.impl.ModeSetting;

@ModuleAnnotation(
   name = "Health Resolver",
   category = Category.MISC,
   description = "Определяет HP игроков через скорборд"
)
public final class HealthResolver extends Module {
   public static final HealthResolver INSTANCE = new HealthResolver();

   private final ModeSetting mode = new ModeSetting("Mode", new String[0]);
   private final ModeSetting.Value reallyWorld;
   private final ModeSetting.Value funTime;

   public HealthResolver() {
      this.reallyWorld = new ModeSetting.Value(this.mode, "Really World");
      this.funTime = new ModeSetting.Value(this.mode, "Fun Time").select();
   }

   public boolean isRW() {
      return reallyWorld.isSelected() && this.isEnabled();
   }

   public boolean isFT() {
      return funTime.isSelected() && this.isEnabled();
   }

   @EventTarget
   public void onTick(EventTick event) {
      if (!isFT()) return;
      if (mc.getNetworkHandler() == null || mc.getNetworkHandler().getServerInfo() == null) return;

      for (PlayerEntity player : mc.world.getPlayers()) {
         if (player == mc.player) continue;
         if (player.getName() == null) continue;
         
         String playerName = player.getName().getString();
         // Skip invalid names (bots, etc.)
         if (playerName.isEmpty() || playerName.length() > 16) continue;

         ScoreboardObjective scoreboard = null;
         String parsedHealth = "";
         
         if (player.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME) != null) {
            scoreboard = player.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME);
            if (scoreboard != null) {
               ReadableScoreboardScore readableScoreboardScore = player.getScoreboard().getScore(player, scoreboard);
               MutableText mutableText = ReadableScoreboardScore.getFormattedScore(readableScoreboardScore, scoreboard.getNumberFormatOr(StyledNumberFormat.EMPTY));
               parsedHealth = mutableText.getString();
            }
         }
         
         float resolvedHealth = 0f;
         try {
            resolvedHealth = Float.parseFloat(parsedHealth);
         } catch (NumberFormatException ignored) {}

         if (!parsedHealth.isEmpty() && !parsedHealth.equals("0")) {
            player.setHealth(resolvedHealth);
         }
      }
   }
}
