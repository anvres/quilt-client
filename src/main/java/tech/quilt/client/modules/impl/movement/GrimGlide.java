package tech.quilt.client.modules.impl.movement;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import tech.quilt.base.events.impl.other.EventTick;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.client.modules.api.setting.impl.NumberSetting;

@ModuleAnnotation(
   name = "GrimGlide",
   category = Category.MOVEMENT,
   description = "Legit Elytra Boost для Grim/ReallyWorld"
)
public final class GrimGlide extends Module {
   public static final GrimGlide INSTANCE = new GrimGlide();

   // Слайдер скорости, подстроенный под лимиты Grim (в районе 45-50 BPS — это максимум для стабильного нажима)
   private final NumberSetting speedTarget = new NumberSetting("Speed", 45.0F, 10.0F, 120.0F, 1.0F);
   private int ticksTwo = 0;

   @Override
   public void onEnable() {
      ticksTwo = 0;
      super.onEnable();
   }

   @EventTarget
   public void onTick(EventTick event) {
      // Базовые проверки на null и полет на элитрах
      if (mc.player == null || mc.world == null || !mc.player.isGliding()) return;

      ticksTwo++;
      Vec3d pos = mc.player.getPos();
      float yaw = mc.player.getYaw();

      // Легитный шаг движения вперед
      double forward = 0.085;

      // FIX: Считаем BPS напрямую через дельту позиции
      double dX = mc.player.getX() - mc.player.prevX;
      double dZ = mc.player.getZ() - mc.player.prevZ;
      double currentBps = Math.sqrt(dX * dX + dZ * dZ) * 20.0;

      // Ограничитель скорости из настроек
      float maxSpeed = speedTarget.getCurrent();
      if (currentBps >= maxSpeed) {
         forward = 0.0;
      }

      // Переводим углы в радианы
      double radYaw = Math.toRadians(yaw);
      double dx = -Math.sin(radYaw) * forward;
      double dz = Math.cos(radYaw) * forward;

      // Настройка Y-планирования (пропускает Grim Prediction)
      double motionY = mc.player.getVelocity().y;
      if (mc.options.jumpKey.isPressed()) {
         motionY = 0.32;
      } else if (mc.options.sneakKey.isPressed()) {
         motionY = -0.32;
      } else {
         // Эмуляция микро-покачивания крыльев
         motionY = (ticksTwo % 2 == 0) ? -0.01 : -0.012;
      }

      // Буст работает только при зажатых кнопках движения
      if (mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0) {
         // Рандомный множитель
         double randomMultiplier = 1 + (Math.random() * 0.05);

         // Задаем скорость ОДИН раз за тик
         mc.player.setVelocity(dx * randomMultiplier, motionY, dz * randomMultiplier);

         // Синхронизация позиции с сервером каждую итерацию движения
         if (ticksTwo % 1 == 0) {
            double targetX = pos.getX() + dx;
            double targetY = pos.getY() + motionY;
            double targetZ = pos.getZ() + dz;

            // Шлем пакет позиции, чтобы Grim принял это движение за лаг, а не за чит
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(targetX, targetY, targetZ, false, false));

            // Перемещаем клиента
            mc.player.setPosition(targetX, targetY, targetZ);
         }
      } else {
         // Мягкое гашение скорости, если игрок бросил управление
         mc.player.setVelocity(0, motionY, 0);
      }
   }
}
