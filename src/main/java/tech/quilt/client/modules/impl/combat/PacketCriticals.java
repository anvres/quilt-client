package tech.quilt.client.modules.impl.combat;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import tech.quilt.base.events.impl.player.EventAttack;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.client.modules.api.setting.impl.ModeSetting;
import tech.quilt.client.modules.api.setting.impl.NumberSetting;
import tech.quilt.utility.game.player.PlayerIntersectionUtil;

import java.util.concurrent.ThreadLocalRandom;

@ModuleAnnotation(
        name = "PacketCriticals",
        category = Category.COMBAT,
        description = "Критические удары с разными режимами"
)
public final class PacketCriticals extends Module {
    public static final PacketCriticals INSTANCE = new PacketCriticals();

    private final ModeSetting mode = new ModeSetting("Режим", "Post", "Old Holyworld", "KrystalMC", "Grim");
    private final NumberSetting grimRange = new NumberSetting("Разброс", 0.1f, 0.001f, 1.0f, 0.01f);
    private final NumberSetting grimBase = new NumberSetting("Базовый оффсет", 0.02f, 0.001f, 1.0f, 0.01f);

    @EventTarget
    public void onAttack(EventAttack event) {
        if (mc.player == null || mc.world == null) return;

        if (mode.is("Old Holyworld")) {
            oldHolyworldCrit();
        } else if (mode.is("KrystalMC")) {
            krystalMCCrit();
        } else if (mode.is("Grim")) {
            Entity target = event.getTarget();
            if (target == null || target instanceof ItemFrameEntity) return;
            grimCrit();
        }
    }

    private void oldHolyworldCrit() {
        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y - 0.0625, z, false, false));
    }

    private void krystalMCCrit() {
        if (!mc.player.hasStatusEffect(StatusEffects.LEVITATION)) {
            if (!isInLiquid()) return;
        }
        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y - 0.0625, z, false, false));
    }

    private void grimCrit() {
        LivingEntity target = Aura.INSTANCE.isEnabled() ? Aura.INSTANCE.getTarget() : null;
        if (target == null) return;

        if (mc.player.isOnGround()) return;

        double playerY = mc.player.getY();
        if (playerY != (int) playerY) {
            boolean inLiquid = isInLiquid();
            boolean inWall = PlayerIntersectionUtil.isPlayerInBlock(Blocks.COBWEB);
            if (inLiquid || inWall) {
                float offset = ThreadLocalRandom.current().nextFloat() * grimRange.getCurrent() + grimBase.getCurrent();
                mc.player.fallDistance = offset;
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
                        mc.player.getX(), playerY - offset, mc.player.getZ(),
                        mc.player.getYaw(), mc.player.getPitch(), false, false
                ));
            }
        }
    }

    private boolean isInLiquid() {
        if (mc.player == null || mc.world == null) return false;
        Box box = mc.player.getBoundingBox();
        for (BlockPos pos : BlockPos.iterate(
                MathHelper.floor(box.minX), MathHelper.floor(box.minY), MathHelper.floor(box.minZ),
                MathHelper.floor(box.maxX), MathHelper.floor(box.maxY), MathHelper.floor(box.maxZ)
        )) {
            if (mc.world.getBlockState(pos).isOf(Blocks.WATER)) return true;
        }
        return false;
    }

    public boolean isCritting() {
        return isEnabled() && mc.player != null &&
                (mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING) || isInLiquid());
    }
}
