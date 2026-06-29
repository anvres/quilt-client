package tech.quilt.client.modules.impl.movement;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import tech.quilt.base.events.impl.other.EventTick;
import tech.quilt.base.events.impl.player.EventMotion;
import tech.quilt.base.events.impl.player.EventOnTravelPost;
import tech.quilt.base.events.impl.player.EventUpdate;
import tech.quilt.base.events.impl.server.EventPacket;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.client.modules.api.setting.impl.BooleanSetting;
import tech.quilt.client.modules.api.setting.impl.ModeSetting;
import tech.quilt.client.modules.api.setting.impl.NumberSetting;
import tech.quilt.utility.game.player.MovingUtil;

@ModuleAnnotation(
   name = "Speed",
   category = Category.MOVEMENT,
   description = "Увеличивает скорость передвижения"
)
public final class Speed extends Module {
   public static final Speed INSTANCE = new Speed();

   private final ModeSetting mode = new ModeSetting("Mode", "Vanilla", "Grim", "ReallyWorld", "GrimTeleport", "Matrix", "HolyWorld", "MetaHvH");
   private final NumberSetting speed = new NumberSetting("Speed", 1.0F, 0.1F, 5.0F, 0.1F, () -> mode.is("Vanilla"));
   private final ModeSetting grimType = new ModeSetting("Grim mode", () -> mode.is("Grim"), "Collide", "Collide new");
   private final NumberSetting collideDistance = new NumberSetting("Collide distance", 1.5F, 0.1F, 3.0F, 0.01F, () -> mode.is("Grim"));
   private final NumberSetting speedValue = new NumberSetting("Speed value", 0.15F, 0.1F, 1.0F, 0.01F, () -> mode.is("Grim"));

   private final NumberSetting holySpeed = new NumberSetting("Holy speed", 0.35F, 0.2F, 1.0F, 0.05F, () -> mode.is("HolyWorld"));
   private final NumberSetting holyRange = new NumberSetting("Holy range", 0.35F, 0.2F, 0.95F, 0.01F, () -> mode.is("HolyWorld"));

   private final BooleanSetting metaDamage = new BooleanSetting("Damage boost", false, () -> mode.is("MetaHvH"));

   private int tick;
   private boolean boostActive;

   private Speed() {
   }

   @Override
   public void onEnable() {
      tick = 0;
      boostActive = false;
      super.onEnable();
   }

   @Override
   public void onDisable() {
      tick = 0;
      boostActive = false;
      super.onDisable();
   }

   @EventTarget
   public void onMotion(EventMotion event) {
      if (mc.player == null || mc.world == null) return;

      if (mode.is("Vanilla") && MovingUtil.hasPlayerMovement() && !mc.player.isGliding()) {
         MovingUtil.setVelocity(speed.getCurrent());
      }

      if (mode.is("ReallyWorld") && mc.player.isOnGround()) {
         tick++;
         if (tick >= 1) {
            mc.player.jump();
         }
      }

      if (mode.is("GrimTeleport")) {
         if (tick > 3) {
            double s = 0.03;
            if (tick % 2 == 0) {
               mc.player.setVelocity(new Vec3d(0, 0.03, 0));
               if (mc.player.isOnGround()) s = 0.0855;
            }
            if (MovingUtil.hasPlayerMovement()) {
               double yaw = Math.toRadians(MovingUtil.direction(mc.player.getYaw(), mc.player.input.movementForward, mc.player.input.movementSideways));
               mc.player.setVelocity(new Vec3d(-Math.sin(yaw) * s, 0, Math.cos(yaw) * s));
            }
         }
         tick++;
      }
   }

   @EventTarget
   public void onTravel(EventOnTravelPost event) {
      if (mc.player == null || mc.world == null) return;

      if (mode.is("Grim")) {
         if (grimType.is("Collide") || grimType.is("Collide new")) {
            boolean newMode = grimType.is("Collide new");
            int collisions = 0;

            for (Entity entity : mc.world.getEntities()) {
               if (entity instanceof LivingEntity living) {
                  if (living == mc.player) continue;
                  if (living instanceof ArmorStandEntity) continue;
                  if (hasCollisionWith(living, newMode ? 0f : 1f)) {
                     collisions++;
                  }
               }
            }

            if (collisions > 0) {
               double[] forward = MovingUtil.calculateDirection(0.08 * collisions);
               mc.player.addVelocity(forward[0], 0.0, forward[1]);
            }
         }
      }

      if (mode.is("HolyWorld")) {
         if (mc.player.getAbilities().flying) return;
         if (!MovingUtil.hasPlayerMovement() || mc.player.isOnGround()) return;

         float range = holyRange.getCurrent();
         Box box = mc.player.getBoundingBox().expand(range, 0.1, range);
         int collisions = 0;

         for (PlayerEntity player : mc.world.getPlayers()) {
            if (player != mc.player && player.isAlive() && box.intersects(player.getBoundingBox())) {
               collisions++;
            }
         }

         if (collisions > 0) {
            double val = Math.max(0.0, holySpeed.getCurrent() / collisions);
            double[] dir = MovingUtil.calculateDirection(val);
            mc.player.addVelocity(dir[0], 0.0, dir[1]);
         }
      }

      if (mode.is("Matrix")) {
         if (mc.player.getVelocity().y == -0.4448259643949201) {
            mc.player.setVelocity(
               mc.player.getVelocity().x * 2.0,
               mc.player.getVelocity().y,
               mc.player.getVelocity().z * 2.0
            );
         }
      }
   }

   @EventTarget
   public void onTick(EventTick event) {
      if (mc.player == null) return;

      if (mode.is("MetaHvH")) {
         if (metaDamage.isEnabled() && mc.player.hurtTime > 0) {
            boostActive = true;
         }
         if (boostActive && mc.player.hurtTime <= 0) {
            boostActive = false;
         }
         MovingUtil.setVelocity(boostActive ? 0.55F : 0.2F);
      }
   }

   @EventTarget
   public void onPacket(EventPacket event) {
      if (!event.isReceive()) return;
      if (mc.player == null) return;

      if (mode.is("ReallyWorld") && event.getPacket() instanceof PlayerMoveC2SPacket) {
         if (tick % 2 == 0) {
            mc.player.setVelocity(mc.player.getVelocity().add(0, 0.4, 0));
         }
      }
   }

   private boolean hasCollisionWith(Entity entity, float expand) {
      return mc.player.getBoundingBox().expand(expand).intersects(entity.getBoundingBox());
   }
}
