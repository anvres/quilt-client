package tech.quilt.client.modules.impl.combat;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.*;
import tech.quilt.Quilt;
import tech.quilt.base.events.impl.other.EventTick;
import tech.quilt.base.events.impl.player.EventUpdate;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.client.modules.api.setting.impl.BooleanSetting;
import tech.quilt.client.modules.api.setting.impl.ModeSetting;
import tech.quilt.client.modules.api.setting.impl.MultiBooleanSetting;
import tech.quilt.client.modules.api.setting.impl.NumberSetting;
import tech.quilt.utility.game.player.PlayerInventoryUtil;
import tech.quilt.utility.game.player.RaytracingUtil;
import tech.quilt.utility.game.player.rotation.Rotation;
import tech.quilt.utility.game.player.rotation.RotationUtil;
import tech.quilt.utility.math.Timer;

import java.util.List;

@ModuleAnnotation(
   name = "AutoCrystal",
   category = Category.COMBAT,
   description = "Автоматически ставит и бьет кристаллы"
)
public final class AutoCrystal extends Module {
   public static final AutoCrystal INSTANCE = new AutoCrystal();

   private final ModeSetting aimMode = new ModeSetting("Aim Mode", new String[0]);
   private final ModeSetting.Value instant;
   private final ModeSetting.Value lonyGrief;
   private final NumberSetting distance = new NumberSetting("Distance", 3.0f, 2.5f, 6.0f, 0.1f);
   private final NumberSetting placeDelay = new NumberSetting("Place Delay", 2.0f, 0.0f, 20.0f, 1.0f);
   private final NumberSetting attackDelay = new NumberSetting("Attack Delay", 5.0f, 0.0f, 20.0f, 1.0f);
   private final MultiBooleanSetting options = MultiBooleanSetting.create("Options", List.of("Raytrace", "No Suicide", "Delayed Swap Back"));
   private final NumberSetting swapBackDelay = new NumberSetting("Swap Back Delay", 5.0f, 1.0f, 20.0f, 1.0f, () -> options.isEnable("Delayed Swap Back"));

   private final Timer placeTimer = new Timer();
   private final Timer attackTimer = new Timer();
   private final Timer swapBackTimer = new Timer();

   private Entity crystalEntity = null;
   private BlockPos obsidianPos = null;

   private int prevSlot = -1;
   private int currentSlot = -1;
   private int bestSlot = -1;
   private boolean swapBack = false;

   private Runnable placeRunnable = null;

   public AutoCrystal() {
      this.instant = new ModeSetting.Value(this.aimMode, "Instant").select();
      this.lonyGrief = new ModeSetting.Value(this.aimMode, "LonyGrief");
   }

   @Override
   public void onEnable() {
      reset();
      super.onEnable();
   }

   @Override
   public void onDisable() {
      reset();
      super.onDisable();
   }

   @EventTarget
   public void onTick(EventTick event) {
      handleTickEvent();
   }

   @EventTarget
   public void onUpdate(EventUpdate event) {
      // Handle block place from inventory
      if (mc.interactionManager.isBreakingBlock()) return;
      
      BlockHitResult hitResult = mc.crosshairTarget instanceof BlockHitResult ? (BlockHitResult) mc.crosshairTarget : null;
      if (hitResult != null && hitResult.getBlockPos() != null) {
         BlockPos pos = hitResult.getBlockPos();
         if (mc.world.getBlockState(pos).getBlock() == Blocks.OBSIDIAN || 
             mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK) {
            handlePlaceEvent(pos);
         }
      }
   }

   private void handlePlaceEvent(BlockPos pos) {
      obsidianPos = pos;
      boolean isOffhand = mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL;

      int slotInv = PlayerInventoryUtil.find(Items.END_CRYSTAL, 9, 36);
      int slotHb = PlayerInventoryUtil.find(Items.END_CRYSTAL, 0, 8);
      bestSlot = findEmptyHotbarSlot();

      if (options.isEnable("Delayed Swap Back") && mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL) {
         swapBackTimer.reset();
      }

      if (isOffhand) {
         if (obsidianPos != null) {
            placeRunnable = () -> placeCrystal(bestSlot, obsidianPos);
            placeTimer.reset();
         }
      } else if (slotHb == -1 && slotInv != -1 && bestSlot != -1) {
         placeRunnable = () -> funnyBabah(slotInv);
         placeTimer.reset();
      } else if (slotHb != -1) {
         placeRunnable = () -> {
            if (obsidianPos == null) return;
            
            prevSlot = mc.player.getInventory().selectedSlot;
            placeCrystal(slotHb, obsidianPos);

            if (options.isEnable("Delayed Swap Back")) {
               swapBackTimer.reset();
               swapBack = true;
               currentSlot = mc.player.getInventory().selectedSlot;
            } else {
               mc.player.getInventory().selectedSlot = prevSlot;
            }
         };
         placeTimer.reset();
      }
   }

   private void handleTickEvent() {
      if (crystalEntity != null && !crystalEntity.isAlive()) {
         reset();
      }

      if (placeRunnable != null && placeTimer.finished((long)(placeDelay.getCurrent() * 50))) {
         placeRunnable.run();
         placeTimer.reset();
         placeRunnable = null;
      }

      if (obsidianPos != null && attackTimer.finished((long)(attackDelay.getCurrent() * 50))) {
         for (EndCrystalEntity crystal : findCrystals(obsidianPos)) {
            if (isValid(crystal)) {
               attackCrystal(crystal);
            }
         }
      }

      if (options.isEnable("Delayed Swap Back") && swapBack) {
         int playerCurrentSlot = mc.player.getInventory().selectedSlot;

         if (playerCurrentSlot != currentSlot && playerCurrentSlot != prevSlot) {
            swapBack = false;
            return;
         }

         if (swapBackTimer.finished((long)(swapBackDelay.getCurrent() * 50))) {
            mc.player.getInventory().selectedSlot = prevSlot;
            swapBack = false;
         }
      }
   }

   private void attackCrystal(Entity entity) {
      if (!isValid(entity) || mc.player.getAttackCooldownProgress(1.0f) < 1.0f) return;

      Rotation rotations = rotate(entity);
      boolean successRaytrace = true;
      
      if (options.isEnable("Raytrace")) {
         EntityHitResult hitResult = RaytracingUtil.raytraceEntity(distance.getCurrent(), rotations, e -> e == entity);
         successRaytrace = hitResult != null && hitResult.getEntity() == entity;
      }

      if (successRaytrace || !options.isEnable("Raytrace")) {
         mc.interactionManager.attackEntity(mc.player, entity);
         mc.player.swingHand(Hand.MAIN_HAND);
         attackTimer.reset();
         crystalEntity = entity;
      }

      if (!entity.isAlive()) {
         crystalEntity = null;
         obsidianPos = null;
      }
   }

   private Rotation rotate(Entity entity) {
      Vec3d targetPos = entity.getBoundingBox().getCenter();
      return RotationUtil.fromVec3d(targetPos.subtract(mc.player.getEyePos()));
   }

   private void placeCrystal(int slot, BlockPos pos) {
      boolean isOffhand = mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL;
      Vec3d center = Vec3d.ofCenter(pos);
      BlockHitResult hitResult = new BlockHitResult(center, Direction.UP, pos, false);

      if (isOffhand) {
         if (mc.interactionManager.interactBlock(mc.player, Hand.OFF_HAND, hitResult).isAccepted()) {
            mc.player.swingHand(Hand.OFF_HAND);
         }
      } else {
         if (slot >= 0 && slot < 9) {
            mc.player.getInventory().selectedSlot = slot;
         }
         if (mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult).isAccepted() &&
                 mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL) {
            mc.player.swingHand(Hand.MAIN_HAND);
         }
      }
   }

   private boolean isValid(Entity entity) {
      if (entity == null || obsidianPos == null || !entity.isAlive()) {
         return false;
      }

      if (options.isEnable("No Suicide")) {
         if (mc.player.getY() > obsidianPos.getY()) {
            return false;
         }
      }

      return mc.player.getEyePos().distanceTo(entity.getBoundingBox().getCenter()) < distance.getCurrent();
   }

   private List<EndCrystalEntity> findCrystals(BlockPos pos) {
      return mc.world.getEntitiesByClass(
         EndCrystalEntity.class,
         new Box(pos).expand(1.0, 2.0, 1.0),
         endCrystalEntity -> endCrystalEntity != null && endCrystalEntity.isAlive()
      );
   }

   private void funnyBabah(int slot) {
      if (bestSlot == -1) return;
      
      // Move crystal to hotbar
      if (slot >= 9 && slot < 36 && bestSlot >= 0 && bestSlot < 9) {
         mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, bestSlot, SlotActionType.SWAP, mc.player);
      }
      
      if (obsidianPos != null) {
         prevSlot = mc.player.getInventory().selectedSlot;
         placeCrystal(bestSlot, obsidianPos);

         if (options.isEnable("Delayed Swap Back")) {
            swapBackTimer.reset();
            swapBack = true;
            currentSlot = mc.player.getInventory().selectedSlot;
         } else {
            mc.player.getInventory().selectedSlot = prevSlot;
         }
      }
      
      // Swap back
      if (slot >= 9 && slot < 36 && bestSlot >= 0 && bestSlot < 9) {
         mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, bestSlot, slot, SlotActionType.SWAP, mc.player);
      }
   }

   private void reset() {
      crystalEntity = null;
      obsidianPos = null;
      prevSlot = -1;
      bestSlot = -1;
      swapBack = false;
      currentSlot = -1;

      placeTimer.reset();
      attackTimer.reset();
      swapBackTimer.reset();

      placeRunnable = null;
   }

   private int findEmptyHotbarSlot() {
      for (int i = 0; i < 9; i++) {
         if (mc.player.getInventory().getStack(i).isEmpty()) {
            return i;
         }
      }
      return -1;
   }
}
