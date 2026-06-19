package tech.quilt.client.modules.impl.misc;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.util.Hand;
import tech.quilt.base.events.impl.other.EventTick;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.client.modules.api.setting.impl.BooleanSetting;
import tech.quilt.client.modules.api.setting.impl.MultiBooleanSetting;
import tech.quilt.client.modules.api.setting.impl.NumberSetting;
import tech.quilt.utility.math.Timer;

import java.util.List;

@ModuleAnnotation(
   name = "TapeMouse",
   category = Category.MISC,
   description = "Автоматическое нажатие кнопок мыши с задержкой"
)
public final class TapeMouse extends Module {
   public static final TapeMouse INSTANCE = new TapeMouse();

   private final MultiBooleanSetting actions = MultiBooleanSetting.create("Actions", List.of("Attack", "Use"));
   private final NumberSetting attackDelay = new NumberSetting("Attack Delay", 10.0f, 1.0f, 20.0f, 1.0f, "Задержка атаки (тиков)");
   private final NumberSetting useDelay = new NumberSetting("Use Delay", 10.0f, 1.0f, 20.0f, 1.0f, "Задержка использования (тиков)");

   private final Timer attackTimer = new Timer();
   private final Timer useTimer = new Timer();

   public TapeMouse() {
      attackDelay.setVisible(() -> actions.isEnable("Attack"));
      useDelay.setVisible(() -> actions.isEnable("Use"));
   }

   @EventTarget
   public void onTick(EventTick event) {
      if (actions.isEnable("Attack")) {
         handleAction(attackDelay.getCurrent(), attackTimer, () -> {
            mc.interactionManager.attackEntity(mc.player, mc.targetedEntity);
            mc.player.swingHand(Hand.MAIN_HAND);
         });
      }
      
      if (actions.isEnable("Use")) {
         handleAction(useDelay.getCurrent(), useTimer, () -> {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
         });
      }
   }

   private void handleAction(float delay, Timer timer, Runnable run) {
      if (timer.finished((long) (delay * 50))) {
         run.run();
         timer.reset();
      }
   }
}
