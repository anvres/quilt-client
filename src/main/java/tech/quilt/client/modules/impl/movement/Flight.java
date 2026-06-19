package tech.quilt.client.modules.impl.movement;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import tech.quilt.base.events.impl.player.EventMotion;
import tech.quilt.base.events.impl.player.EventUpdate;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.client.modules.api.setting.impl.ModeSetting;
import tech.quilt.client.modules.api.setting.impl.NumberSetting;
import tech.quilt.utility.game.player.MovingUtil;
import tech.quilt.utility.game.player.PlayerInventoryUtil;
import tech.quilt.utility.math.Timer;

@ModuleAnnotation(
        name = "Flight",
        category = Category.MOVEMENT,
        description = "Полет"
)
public final class Flight extends Module {
    public static final Flight INSTANCE = new Flight();
    private final ModeSetting mode = new ModeSetting("Тип", "Motion", "Motion", "ElytraRWOld");
    private final NumberSetting xSpeed = new NumberSetting("X - Скорость", 1.0F, 0.0F, 5.0F, 0.1F);
    private final NumberSetting ySpeed = new NumberSetting("Y - Скорость", 1.0F, 0.0F, 5.0F, 0.1F);
    private final Timer timer = new Timer();
    private int itemSlot = -1;

    private Flight() {
    }

    @EventTarget
    public void onEvent(EventMotion event) {
        if (this.mode.is("Motion")) {
            double y = 0.0;
            if (mc.options.jumpKey.isPressed()) {
                y = this.ySpeed.getCurrent();
            } else if (mc.options.sneakKey.isPressed()) {
                y = -this.ySpeed.getCurrent();
            }
            mc.player.setVelocity(0, y, 0);
            if (mc.options.sprintKey.isPressed()) {
                MovingUtil.setVelocity(this.xSpeed.getCurrent());
            }
        }
    }

    @EventTarget
    public void onEvent(EventUpdate event) {
        if (this.mode.is("ElytraRWOld")) {
            for (int i = 0; i < 9; ++i) {
                if (mc.player.getInventory().getStack(i).isOf(Items.ELYTRA) && !mc.player.isOnGround() && !mc.player.isSubmergedInWater() && !mc.player.isInLava() && !mc.player.isGliding()) {
                    int swapDelay = 520;
                    if (this.timer.finished(swapDelay)) {
                        PlayerInventoryUtil.swapAndUseLegit(Items.FIREWORK_ROCKET);
                        mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                        mc.player.startGliding();
                        itemSlot = i;
                        this.timer.reset();
                    }

                    if (mc.player.isGliding()) {
                        PlayerInventoryUtil.swapAndUseHvH(Items.FIREWORK_ROCKET);
                    }
                }
            }
        }
    }
}
