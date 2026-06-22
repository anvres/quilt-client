package tech.quilt.client.modules.impl.combat;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import tech.quilt.base.events.impl.other.EventGameUpdate;
import tech.quilt.base.player.AttackUtil;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.client.modules.api.setting.impl.BooleanSetting;
import tech.quilt.client.modules.api.setting.impl.MultiBooleanSetting;
import tech.quilt.client.modules.api.setting.impl.NumberSetting;
import tech.quilt.utility.game.player.TargetSelector;
import tech.quilt.utility.math.Timer;
import tech.quilt.utility.interfaces.IMinecraft;

import java.util.List;

@ModuleAnnotation(
        name = "TriggerBot",
        category = Category.COMBAT,
        description = "Автоатака при наведении на цель"
)
public final class TriggerBot extends Module implements IMinecraft {

    public static final TriggerBot INSTANCE = new TriggerBot();
    private final Timer attackTimer = new Timer();
    private TriggerBot() {}

    private final BooleanSetting onlyCrits = new BooleanSetting("Только криты", false);

    private final BooleanSetting smartCrits = new BooleanSetting("Умные криты", "Бьёт критом только при зажатом прыжке", true, onlyCrits::isEnabled);

    private final BooleanSetting eatAttack = new BooleanSetting("Бить и есть", true);

    private final NumberSetting delay = new NumberSetting("Задержка между атаками (мс)", 0.0f, 0.0f, 500.0f, 10.0f);

    private final MultiBooleanSetting targets = MultiBooleanSetting.create(
            "Цели",
            List.of("Игроков", "Мобов", "Животных")
    );

    @EventTarget
    public void onRotate(EventGameUpdate e) {
        if (mc.player == null || mc.world == null) return;
        
        if (mc.player.isUsingItem() && !eatAttack.isEnabled()) return;
        
        if (mc.player.getAttackCooldownProgress(1.0f) < 0.92f) return;
        
        if (!attackTimer.finished((long) delay.getCurrent())) return;
        
        if (mc.player.getAbilities().creativeMode) return;
        
        boolean isCrit = AttackUtil.isPlayerInCriticalState();
        
        if (onlyCrits.isEnabled() && !isCrit) {
            if (smartCrits.isEnabled() && !mc.options.jumpKey.isPressed()) {
                return;
            }
            if (!smartCrits.isEnabled()) {
                return;
            }
        }

        LivingEntity target = getMouseOverTarget();
        if (target == null) return;

        AttackUtil.attackEntity(target);
        attackTimer.reset();
    }
    
    private LivingEntity getMouseOverTarget() {
        if (mc.targetedEntity != null && mc.targetedEntity instanceof LivingEntity living) {
            if (isValidTarget(living)) {
                return living;
            }
        }
        return null;
    }

    private boolean isValidTarget(Entity entity) {
        if (!(entity instanceof LivingEntity living)) return false;
        if (entity == mc.player) return false;
        if (!entity.isAlive()) return false;

        TargetSelector.EntityFilter filter = new TargetSelector.EntityFilter(targets.getSelectedNames());
        return filter.isValid(living);
    }
}
