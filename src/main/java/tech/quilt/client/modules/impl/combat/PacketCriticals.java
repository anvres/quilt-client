package tech.quilt.client.modules.impl.combat;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import tech.quilt.base.events.impl.player.EventAttack;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.utility.game.player.PlayerIntersectionUtil;

@ModuleAnnotation(
   name = "PacketCriticals",
   category = Category.COMBAT,
   description = "Бьет критами в паутине / под эффектом плавного падения"
)
public final class PacketCriticals extends Module {
   public static final PacketCriticals INSTANCE = new PacketCriticals();

   @EventTarget
   public void onAttack(EventAttack event) {
      if (mc.player == null || mc.world == null) return;

      // Проверяем паутину или Slow Falling
      if (!PlayerIntersectionUtil.isPlayerInBlock(Blocks.COBWEB) && 
          !mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING)) return;

      double x = mc.player.getX();
      double y = mc.player.getY();
      double z = mc.player.getZ();

      // Отправляем пакеты для критов
      mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.0625, z, false, false));
      mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.0015, z, false, false));
      mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false, false));
   }
}
