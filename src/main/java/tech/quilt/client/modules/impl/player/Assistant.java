package tech.quilt.client.modules.impl.player;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import tech.quilt.base.events.impl.other.EventTick;
import tech.quilt.base.events.impl.server.EventPacket;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.client.modules.api.setting.impl.BooleanSetting;
import tech.quilt.client.modules.api.setting.impl.ModeSetting;
import tech.quilt.client.modules.api.setting.impl.MultiBooleanSetting;
import tech.quilt.utility.game.player.PlayerInventoryUtil;
import tech.quilt.utility.math.Timer;

import java.util.*;

@ModuleAnnotation(
   name = "Assistant",
   category = Category.PLAYER,
   description = "Помощник для PvP"
)
public final class Assistant extends Module {
   public static final Assistant INSTANCE = new Assistant();

   public enum Mode {
      FUNTIME,
      HOLYWORLD
   }

   private final MultiBooleanSetting functions = MultiBooleanSetting.create("Functions", List.of("Hotkeys", "Timers"));
   private final ModeSetting mode = new ModeSetting("Mode", new String[0]);
   private final ModeSetting.Value funTime;
   private final ModeSetting.Value holyWorld;
   private final BooleanSetting legit = new BooleanSetting("Legit", true);

   private Mode currentMode = Mode.FUNTIME;
   private final Map<Item, Integer> funTimeItems = new HashMap<>();
   private final Map<Item, Integer> holyWorldItems = new HashMap<>();
   private final Timer useTimer = new Timer();

   public Assistant() {
      this.funTime = new ModeSetting.Value(this.mode, "Fun Time").select();
      this.holyWorld = new ModeSetting.Value(this.mode, "Holy World");
      
      mode.setVisible(() -> functions.isEnable("Hotkeys"));
      legit.setVisible(() -> functions.isEnable("Hotkeys"));

      // FunTime items
      funTimeItems.put(Items.ENDER_EYE, -999); // Default key
      funTimeItems.put(Items.NETHERITE_SCRAP, -999);
      funTimeItems.put(Items.SUGAR, -999);
      funTimeItems.put(Items.FIRE_CHARGE, -999);
      funTimeItems.put(Items.DRIED_KELP, -999);
      funTimeItems.put(Items.PHANTOM_MEMBRANE, -999);

      // HolyWorld items
      holyWorldItems.put(Items.PRISMARINE_SHARD, -999);
      holyWorldItems.put(Items.POPPED_CHORUS_FRUIT, -999);
      holyWorldItems.put(Items.NETHER_STAR, -999);
      holyWorldItems.put(Items.FIRE_CHARGE, -999);
   }

   @EventTarget
   public void onTick(EventTick event) {
      if (!functions.isEnable("Hotkeys") || mc.currentScreen != null) return;

      Map<Item, Integer> items = funTime.isSelected() ? funTimeItems : holyWorldItems;
      
      for (Map.Entry<Item, Integer> entry : items.entrySet()) {
         Item item = entry.getKey();
         int slot = PlayerInventoryUtil.find(item, 0, 8);
         
         if (slot != -1 && useTimer.finished(200L)) {
            // Use the item
            mc.player.getInventory().selectedSlot = slot;
            mc.interactionManager.interactItem(mc.player, net.minecraft.util.Hand.MAIN_HAND);
            
            if (legit.isEnabled()) {
               // Return to previous slot
               mc.player.getInventory().selectedSlot = slot;
            }
            
            useTimer.reset();
            break;
         }
      }
   }

   @EventTarget
   public void onPacket(EventPacket event) {
      if (!functions.isEnable("Timers") || event.isSent()) return;

      if (event.getPacket() instanceof PlaySoundS2CPacket soundPacket) {
         String soundPath = soundPacket.getSound().getIdAsString();
         
         // Trap sound
         if (soundPath.equals("minecraft:block.piston.contract")) {
            // This is a trap being placed
            // In a real implementation, you'd track the position and show a timer
         } else if (soundPath.equals("minecraft:block.anvil.place")) {
            // Anvil trap
            // Track anvil trap position
         }
      }
   }

   public boolean isRW() {
      return holyWorld.isSelected() && this.isEnabled();
   }

   public boolean isFT() {
      return funTime.isSelected() && this.isEnabled();
   }
}
