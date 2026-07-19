package tech.quilt.client.modules.impl.combat;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import tech.quilt.Quilt;
import tech.quilt.base.events.impl.other.EventGameUpdate;
import tech.quilt.base.events.impl.other.EventTick;
import tech.quilt.base.events.impl.other.EventTickMovement;
import tech.quilt.base.events.impl.player.EventMoveInput;
import tech.quilt.base.events.impl.player.EventRotation;
import tech.quilt.base.events.impl.player.EventUpdate;
import tech.quilt.base.events.impl.render.EventRender3D;
import tech.quilt.base.player.AttackUtil;
import tech.quilt.base.request.ScriptManager;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.client.modules.api.setting.impl.BooleanSetting;
import tech.quilt.client.modules.api.setting.impl.ModeSetting;
import tech.quilt.client.modules.api.setting.impl.MultiBooleanSetting;
import tech.quilt.client.modules.api.setting.impl.NumberSetting;
import tech.quilt.client.modules.impl.combat.rotation.*;
import tech.quilt.client.modules.impl.movement.AirStuck;
import tech.quilt.client.modules.impl.movement.AutoSprint;
import tech.quilt.client.modules.impl.movement.ElytraSample;
import tech.quilt.utility.component.FreeLookComponent;
import tech.quilt.utility.component.RotationComponent;
import tech.quilt.utility.game.player.MovingUtil;
import tech.quilt.utility.game.player.PlayerInventoryUtil;
import tech.quilt.utility.game.player.RaytracingUtil;
import tech.quilt.utility.game.player.rotation.Rotation;
import tech.quilt.utility.game.player.rotation.RotationUtil;
import tech.quilt.utility.math.MultipointUtils;
import tech.quilt.utility.math.Timer;
import tech.quilt.utility.predict.PredictUtils;
import tech.quilt.utility.render.level.Render3DUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@ModuleAnnotation(
        name = "AttackAura",
        category = Category.COMBAT,
        description = "Автоматически бьет цель"
)
public final class Aura extends Module {
    public static final Aura INSTANCE = new Aura();
    private final MultiBooleanSetting targetTypeSetting = MultiBooleanSetting.create("Атаковать", List.of("Игроков", "Мобов", "Животных"));
    public final ModeSetting rotationMode = new ModeSetting("Ротация", new String[0]);
    private final MultiBooleanSetting targetPriority = MultiBooleanSetting.create("Приоритет", List.of("Здоровье", "Дистанция", "Зрение"));
    private final BooleanSetting predictOnElytra = new BooleanSetting("Перегонять противника", true);

    private final ModeSetting.Value modeVanilla;
    private final ModeSetting.Value modeFunTime;
    private final ModeSetting.Value modeUniversal;
    private final ModeSetting.Value modeSloth1;
    private final ModeSetting.Value modeSloth2;
    private final ModeSetting.Value modeReallyWorld;
    private final ModeSetting.Value modeHVH;
    private final ModeSetting.Value modeLonyJir;
    private final ModeSetting.Value modeLegendsGrief;
    private final ModeSetting.Value modeGrim;
    private final ModeSetting.Value modeHuman;

    private final VanillaRotation rotVanilla = new VanillaRotation();
    private final FunTimeRotation rotFunTime = new FunTimeRotation();
    private final UniversalRotation rotUniversal = new UniversalRotation();
    private final Sloth1Rotation rotSloth1 = new Sloth1Rotation();
    private final Sloth2Rotation rotSloth2 = new Sloth2Rotation();
    private final ReallyWorldRotation rotReallyWorld = new ReallyWorldRotation();
    private final HVHRotation rotHVH = new HVHRotation();
    private final LonyJirRotation rotLonyJir = new LonyJirRotation();
    private final LegendsGriefRotation rotLegendsGrief = new LegendsGriefRotation();
    private final GrimRotation rotGrim = new GrimRotation();
    private final HumanRotation rotHuman = new HumanRotation();

    private final ModeSetting correction;
    private final ModeSetting.Value correctionFocus;
    private final ModeSetting.Value correctionGood;
    private final ModeSetting.Value correctionNone;
    private final NumberSetting distance;
    private final NumberSetting distanceRotation;
    private final BooleanSetting shieldBreak;
    private final BooleanSetting legitSwap;
    private final BooleanSetting raycastCheck;
    private final BooleanSetting doubleAttack;
    public final BooleanSetting critsOnlyWithSpace;
    public final BooleanSetting smartCrits;
    private final BooleanSetting visualElytraRotation;
    private final BooleanSetting visualBackTurn = new BooleanSetting("Визуальный разворот", true, () -> this.predictOnElytra.isEnabled() && tech.quilt.client.modules.impl.movement.ElytraSample.INSTANCE.predictionType.is("По смещению хитбокса"));
    private final BooleanSetting visualizePrediction = new BooleanSetting("Визуализация предсказания", true);
    private final BooleanSetting showTargetHitbox = new BooleanSetting("Показывать хитбокс цели", true);
    private final BooleanSetting skipInvisible = new BooleanSetting("Не бить инвизок", true);
    private final BooleanSetting keepTarget;
    private final BooleanSetting sprintReset;

    private LivingEntity target;
    private Vec3d lastPredictedPoint;
    private final Timer hurtTimer;
    private final ScriptManager.ScriptTask script;
    private int lastSlot;
    public float lastYaw;
    public float lastPitch;
    private int postAttackTicks = 0;
    private boolean needSprintReset = false;
    private boolean sprintResetDone = false;
    private int sprintResetTicks = 0;
    private int targetLostTicks = 0;
    private Vec3d smoothedAimPoint;
    private Vec3d smoothedTargetVelocity = Vec3d.ZERO;
    private float visualLookYaw;
    private float visualLookPitch;
    private boolean visualLookInitialized;
    private boolean visualBackTurnEngaged;

    private Aura() {
        this.modeVanilla = new ModeSetting.Value(this.rotationMode, "Vanilla");
        this.modeFunTime = new ModeSetting.Value(this.rotationMode, "FunTime");
        this.modeUniversal = new ModeSetting.Value(this.rotationMode, "Universal (OLD)");
        this.modeSloth1 = new ModeSetting.Value(this.rotationMode, "Sloth1");
        this.modeSloth2 = new ModeSetting.Value(this.rotationMode, "Sloth2");
        this.modeReallyWorld = new ModeSetting.Value(this.rotationMode, "ReallyWorld");
        this.modeHVH = new ModeSetting.Value(this.rotationMode, "HVH");
        this.modeLonyJir = new ModeSetting.Value(this.rotationMode, "LonyJir");
        this.modeLegendsGrief = new ModeSetting.Value(this.rotationMode, "LegendsGrief");
        this.modeGrim = new ModeSetting.Value(this.rotationMode, "Grim");
        this.modeHuman = new ModeSetting.Value(this.rotationMode, "Human");

        this.correction = new ModeSetting("Коррекция", new String[0]);
        this.correctionFocus = new ModeSetting.Value(this.correction, "Фокус");
        this.correctionGood = (new ModeSetting.Value(this.correction, "Свободная")).select();
        this.correctionNone = new ModeSetting.Value(this.correction, "Нет");
        this.distance = new NumberSetting("Дистанция", 3.0F, 0.5F, 6.0F, 0.1F, "Дистанция атаки");
        this.distanceRotation = new NumberSetting("Дистанция аима", 0.1F, 0.0F, 6.0F, 0.1F);
        this.shieldBreak = new BooleanSetting("Ломать щит", true);
        BooleanSetting var10005 = this.shieldBreak;
        Objects.requireNonNull(var10005);
        this.legitSwap = new BooleanSetting("Легитно ломать", true, var10005::isEnabled);
        this.raycastCheck = new BooleanSetting("Проверка на наведение", false);
        this.doubleAttack = new BooleanSetting("Двойной удар", true);
        this.critsOnlyWithSpace = new BooleanSetting("Только с пробелом", true);
        this.smartCrits = new BooleanSetting("Умные криты", false);
        this.visualElytraRotation = new BooleanSetting("Визуальная ротация элитр", true);
        this.keepTarget = new BooleanSetting("Удерживать одну цель", true);
        this.sprintReset = new BooleanSetting("Сброс спринта", true);
        this.target = null;
        this.hurtTimer = new Timer();
        this.script = new ScriptManager.ScriptTask();
        this.lastSlot = -1;
    }
    

    private void breakShieldAndAttack() {
        boolean wasSwapped = false;
        boolean wasSwappedInventory = false;
        int slotHotbar = PlayerInventoryUtil.find(List.of(Items.WOODEN_AXE, Items.STONE_AXE, Items.IRON_AXE, Items.GOLDEN_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE), 0, 8);
        int slotInventory = PlayerInventoryUtil.find(List.of(Items.WOODEN_AXE, Items.STONE_AXE, Items.IRON_AXE, Items.GOLDEN_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE), 8, 35);

        if (this.shouldPrepareSprintReset()) return;

        if (slotHotbar != -1 && this.shieldBreak.isEnabled() && this.target.isBlocking()) {
            if (this.legitSwap.isEnabled()) {
                this.lastSlot = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = slotHotbar;
            } else {
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slotHotbar));
            }
            wasSwapped = true;
        }

        if (slotHotbar == -1 && slotInventory != -1 && this.shieldBreak.isEnabled() && this.target.isBlocking()) {
            if (this.legitSwap.isEnabled()) {
                mc.interactionManager.clickSlot(0, slotInventory, 8, SlotActionType.SWAP, mc.player);
                mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(0));
                this.lastSlot = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = 8;
            } else {
                mc.interactionManager.clickSlot(0, slotInventory, 8, SlotActionType.SWAP, mc.player);
                mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(0));
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slotHotbar));
            }
            wasSwappedInventory = true;
        }

        mc.interactionManager.attackEntity(mc.player, this.target);
        if (this.doubleAttack.isEnabled()) mc.interactionManager.attackEntity(mc.player, this.target);
        mc.player.swingHand(Hand.MAIN_HAND);
        this.postAttackTicks = 7;
        this.resetSprintResetState();

        if (wasSwapped) {
            if (this.legitSwap.isEnabled()) {
                Quilt.getInstance().getScriptManager().addTask(this.script);
                this.script.schedule(EventUpdate.class, (e) -> { mc.player.getInventory().selectedSlot = this.lastSlot; this.lastSlot = -1; return true; });
            } else {
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
            }
        }

        if (wasSwappedInventory) {
            if (this.legitSwap.isEnabled()) {
                Quilt.getInstance().getScriptManager().addTask(this.script);
                this.script.schedule(EventUpdate.class, (e) -> {
                    mc.player.getInventory().selectedSlot = this.lastSlot;
                    mc.interactionManager.clickSlot(0, slotInventory, 8, SlotActionType.SWAP, mc.player);
                    mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(0));
                    this.lastSlot = -1;
                    return true;
                });
            } else {
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
                mc.interactionManager.clickSlot(0, slotInventory, 8, SlotActionType.SWAP, mc.player);
                mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(0));
            }
        }
    }

    @EventTarget
    public void onTick(EventTick e) {
        if (mc.player == null || mc.world == null) return;

        if (this.sprintResetDone) this.sprintResetTicks++;

        LivingEntity newTarget = this.updateTarget();
        if (this.keepTarget.isEnabled() && this.target != null && this.isValid(this.target)) {
            // keep
        } else if (newTarget != null) {
            this.target = newTarget;
            this.targetLostTicks = 0;
            this.rotHuman.setTarget(newTarget);
        } else if (this.target != null && !this.isValid(this.target)) {
            this.targetLostTicks++;
            if (this.targetLostTicks > 5) this.target = null;
        }

        if (this.target != null) {
            if (this.isCanAttack() && this.hurtTimer.finished(458L) && !this.target.isBlocking()) {
                if (this.shouldPrepareSprintReset()) return;
                mc.interactionManager.attackEntity(mc.player, this.target);
                mc.player.swingHand(Hand.MAIN_HAND);
                this.postAttackTicks = 7;
                this.resetSprintResetState();
                this.hurtTimer.reset();
            }
        }

        if (!this.isElytraPredictActive()) this.resetElytraPredictState();
        
        // Обновляем настройки нейронной сети при их изменении
        if (this.modeHuman.isSelected()) {
        }
    }

    @EventTarget
    public void onTickMovement(EventTickMovement e) {
        if (this.target != null) {
            if (this.target.isBlocking() && this.hurtTimer.finished(200L)) {
                this.breakShieldAndAttack();
                this.hurtTimer.reset();
            }
        }
    }

    @EventTarget
    public void eventRotate(EventGameUpdate e) {
        if (this.target != null) {
            Quilt.getInstance().getModuleManager().setAcceleration(0.0F);
            Vec3d eyes = mc.player.getEyePos();
            boolean elytraPredictActive = this.isElytraPredictActive();
            boolean hitboxOffsetMode = tech.quilt.client.modules.impl.movement.ElytraSample.INSTANCE.predictionType.is("По смещению хитбокса");
            boolean useBackVisual = elytraPredictActive && hitboxOffsetMode && this.visualBackTurn.isEnabled();
            boolean elytraVisual = elytraPredictActive && this.visualElytraRotation.isEnabled() && !useBackVisual;

            Vec3d point = MultipointUtils.getMultipoint(this.target, (double) this.distance.getCurrent());

            if (elytraPredictActive) {
                point = elytraSample(eyes);
            }

            this.lastPredictedPoint = point;
            Rotation angle = RotationUtil.fromVec3d(point.subtract(eyes));

            if (useBackVisual) this.updateVisualLook(point);
            else this.resetVisualBackTurn();

            RotationBase currentRot = null;
            if (this.modeVanilla.isSelected()) currentRot = rotVanilla;
            else if (this.modeReallyWorld.isSelected()) currentRot = rotReallyWorld;
            else if (this.modeFunTime.isSelected()) currentRot = rotFunTime;
            else if (this.modeUniversal.isSelected()) currentRot = rotUniversal;
            else if (this.modeSloth1.isSelected()) currentRot = rotSloth1;
            else if (this.modeSloth2.isSelected()) currentRot = rotSloth2;
            else if (this.modeHVH.isSelected()) currentRot = rotHVH;
            else if (this.modeLonyJir.isSelected()) currentRot = rotLonyJir;
            else if (this.modeLegendsGrief.isSelected()) currentRot = rotLegendsGrief;
            else if (this.modeGrim.isSelected()) currentRot = rotGrim;
            else if (this.modeHuman.isSelected()) currentRot = rotHuman;

            if (currentRot != null) {
                currentRot.setYaw(this.lastYaw);
                currentRot.setPitch(this.lastPitch);

                if (currentRot instanceof FunTimeRotation r) r.update(this.target, angle, elytraVisual);
                else if (currentRot instanceof ReallyWorldRotation r) r.update(this.target, angle, elytraVisual);
                else if (currentRot instanceof UniversalRotation r) r.update(this.target, angle, elytraVisual);
                else if (currentRot instanceof Sloth1Rotation r) r.update(this.target, angle, elytraVisual);
                else if (currentRot instanceof Sloth2Rotation r) r.update(this.target, angle, elytraVisual);
                else if (currentRot instanceof LegendsGriefRotation r) r.update(this.target, angle, elytraVisual);
                else if (currentRot instanceof GrimRotation r) r.update(this.target, angle, elytraVisual);
                else if (currentRot instanceof HumanRotation r) r.update(this.target, angle, elytraVisual);
                else currentRot.update(angle, elytraVisual);

                this.lastYaw = currentRot.getYaw();
                this.lastPitch = currentRot.getPitch();
            }
        } else {
            this.resetElytraPredictState();
        }
    }

    private Vec3d elytraSample(Vec3d eyes) {
        float pingFactor = this.getPlayerPing() / 1000.0F;
        ElytraSample es = tech.quilt.client.modules.impl.movement.ElytraSample.INSTANCE;

        Vec3d result = es.sample(this.target, eyes, pingFactor, this.smoothedAimPoint, this.smoothedTargetVelocity);

        if (result != null) {
            // По тикам или По скорости
            this.smoothedAimPoint = result;
            return result;
        }

        // По смещению хитбокса
        double distToTarget = eyes.distanceTo(this.target.getBoundingBox().getCenter());
        float basePrediction = distToTarget > 8.0D ? 8.0F : ElytraSample.INSTANCE.predict.getCurrent();
        float adjustedPrediction = basePrediction + pingFactor * 2.0F;
        Vec3d playerVel = mc.player.getVelocity();
        double relativeSpeed = playerVel.subtract(this.target.getVelocity()).horizontalLength();
        if (relativeSpeed > 1.5) adjustedPrediction += (float) (relativeSpeed * 0.3);
        return this.calculateHitboxOffsetPoint(eyes, pingFactor, adjustedPrediction, distToTarget);
    }

    private boolean isFullyInvisible(LivingEntity entity) {
        for (net.minecraft.item.ItemStack stack : entity.getArmorItems()) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    private boolean isElytraPredictContext() {
        return mc.player != null && mc.player.isGliding() && this.target != null
                && this.target instanceof PlayerEntity && this.target.isGliding();
    }

    private boolean isElytraPredictActive() {
        return this.isEnabled() && this.predictOnElytra.isEnabled() && this.isElytraPredictContext();
    }

    private void resetVisualBackTurn() {
        this.visualLookInitialized = false;
        if (this.visualBackTurnEngaged) {
            FreeLookComponent.setActive(false);
            this.visualBackTurnEngaged = false;
        }
    }

    private void resetElytraPredictState() {
        this.lastPredictedPoint = null;
        this.smoothedAimPoint = null;
        this.smoothedTargetVelocity = Vec3d.ZERO;
        this.resetVisualBackTurn();
    }

    private Vec3d calculateHitboxOffsetPoint(Vec3d eyes, float pingFactor, float adjustedPrediction, double distToTarget) {
        Vec3d velocity = this.target.getVelocity();
        Vec3d playerVel = mc.player.getVelocity();
        Vec3d relativeVel = velocity.subtract(playerVel);
        this.smoothedTargetVelocity = this.smoothedTargetVelocity.multiply(0.55).add(velocity.multiply(0.45));

        double speed = this.smoothedTargetVelocity.horizontalLength();
        double relativeSpeed = relativeVel.horizontalLength();
        Vec3d targetDir;
        if (speed >= 0.05) {
            targetDir = new Vec3d(this.smoothedTargetVelocity.x, this.smoothedTargetVelocity.y * 0.42, this.smoothedTargetVelocity.z).normalize();
        } else {
            targetDir = Vec3d.fromPolar(this.target.getPitch() * 0.35F, this.target.getYaw()).normalize();
        }

        double interceptFactor = MathHelper.clamp(distToTarget / Math.max(relativeSpeed + speed, 0.75), 0.35, 2.5);
        double pingStrength = pingFactor * Math.min(speed * 4.5 + relativeSpeed * 1.15, 5.5);
        double baseStrength = ElytraSample.INSTANCE.predict.getCurrent() * (0.92 + Math.min(distToTarget / 14.0, 0.65));
        double totalStrength = MathHelper.clamp(baseStrength + pingStrength + relativeSpeed * 0.42 + adjustedPrediction * 0.18, 1.25, 10.0);

        Vec3d offset = targetDir.multiply(totalStrength);
        offset = offset.add(relativeVel.multiply(interceptFactor * (0.85 + pingFactor * 1.35)));

        double maxOffset = Math.max(distToTarget * 0.58, 2.0);
        if (offset.length() > maxOffset) offset = offset.normalize().multiply(maxOffset);

        Vec3d rawPoint = this.target.getBoundingBox().getCenter().add(offset);
        if (this.smoothedAimPoint == null) {
            this.smoothedAimPoint = rawPoint;
        } else {
            double smooth = MathHelper.clamp(0.18 + speed * 0.14 + relativeSpeed * 0.06, 0.18, 0.55);
            this.smoothedAimPoint = new Vec3d(
                    MathHelper.lerp(smooth, this.smoothedAimPoint.x, rawPoint.x),
                    MathHelper.lerp(smooth, this.smoothedAimPoint.y, rawPoint.y),
                    MathHelper.lerp(smooth, this.smoothedAimPoint.z, rawPoint.z)
            );
        }
        return this.smoothedAimPoint;
    }

    private void updateVisualLook(Vec3d aimPoint) {
        if (mc.player == null) return;
        Rotation look = RotationUtil.fromVec3d(aimPoint.subtract(mc.player.getEyePos()));
        if (!this.visualLookInitialized) {
            this.visualLookYaw = mc.gameRenderer.getCamera().getYaw();
            this.visualLookPitch = mc.gameRenderer.getCamera().getPitch();
            this.visualLookInitialized = true;
        }

        float deltaYaw = MathHelper.wrapDegrees(look.getYaw() - this.visualLookYaw);
        float deltaPitch = look.getPitch() - this.visualLookPitch;
        float maxStep = 28.0F;
        deltaYaw = MathHelper.clamp(deltaYaw, -maxStep, maxStep);
        deltaPitch = MathHelper.clamp(deltaPitch, -maxStep * 0.65F, maxStep * 0.65F);

        this.visualLookYaw = MathHelper.wrapDegrees(this.visualLookYaw + deltaYaw);
        this.visualLookPitch = MathHelper.clamp(this.visualLookPitch + deltaPitch, -90.0F, 90.0F);

        FreeLookComponent.setActive(true);
        FreeLookComponent.setFreeYaw(this.visualLookYaw);
        FreeLookComponent.setFreePitch(this.visualLookPitch);
        this.visualBackTurnEngaged = true;

        float backYaw = MathHelper.wrapDegrees(this.visualLookYaw + 180.0F);
        mc.player.bodyYaw = backYaw;
        mc.player.headYaw = backYaw;
        mc.player.prevBodyYaw = backYaw;
        mc.player.prevHeadYaw = backYaw;
    }

    @EventTarget
    private void onCameraRotation(EventRotation event) {
        if (!this.isEnabled() || !this.isElytraPredictActive()) return;
        if (!tech.quilt.client.modules.impl.movement.ElytraSample.INSTANCE.predictionType.is("По смещению хитбокса") || !this.visualBackTurn.isEnabled()) return;
        FreeLookComponent.setActive(true);
        event.setYaw(this.visualLookYaw);
        event.setPitch(this.visualLookPitch);
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {
        if (mc.player == null || mc.world == null || target == null) return;
        int color = Quilt.getInstance().getThemeManager().getCurrentTheme().getColor().getRGB();
        if (this.showTargetHitbox.isEnabled()) {
            Render3DUtil.drawBox(target.getBoundingBox(), color, 1.0F);
        }
        if (this.isElytraPredictActive() && this.visualizePrediction.isEnabled() && this.lastPredictedPoint != null) {
            Box box = new Box(
                    lastPredictedPoint.x - 0.3, lastPredictedPoint.y - 0.3, lastPredictedPoint.z - 0.3,
                    lastPredictedPoint.x + 0.3, lastPredictedPoint.y + 0.3, lastPredictedPoint.z + 0.3);
            Render3DUtil.drawBox(box, color, 1.0F);
        }
    }

    @EventTarget
    private void onMoveInput(EventMoveInput eventMoveInput) {
        if (this.needSprintReset) {
            eventMoveInput.setForward(0.0F);
            eventMoveInput.setStrafe(0.0F);
            this.needSprintReset = false;
            this.sprintResetDone = true;
            this.sprintResetTicks = 0;
            mc.player.setSprinting(false);
            return;
        }

        if (!this.correctionNone.isSelected() && this.target != null) {
            if (this.correctionFocus.isSelected()) MovingUtil.fixMovementFocus(eventMoveInput, mc.player.getYaw());
            else MovingUtil.fixMovementFree(eventMoveInput);
        }
    }

    private int getPlayerPing() {
        if (mc.getNetworkHandler() != null && mc.player != null) {
            PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (entry != null) return entry.getLatency();
        }
        return 50;
    }

    private boolean isCanAttack() {
        if (mc.player.getAttackCooldownProgress(0.5F) < 0.9F) return false;
        if (!AttackUtil.canAttack()) return false;

        if (this.critsOnlyWithSpace.isEnabled() && !this.smartCrits.isEnabled() && mc.player.isOnGround()) {
            mc.player.jump();
            return false;
        }

        if (AirStuck.INSTANCE.isEnabled()) {
            return mc.player.getEyePos().distanceTo(this.target.getBoundingBox().getCenter()) <= 6.0;
        } else if (this.target instanceof PlayerEntity && this.predictOnElytra.isEnabled()
                && mc.player.isGliding() && this.target.isGliding()) {
            float pingFactor = this.getPlayerPing() / 1000.0F;
            double extraReach = Math.min(pingFactor * mc.player.getVelocity().length() * 2.5, 2.0);
            double maxDist = this.distance.getCurrent() + extraReach;
            double distToPredicted = mc.player.getEyePos().distanceTo(this.lastPredictedPoint != null ? this.lastPredictedPoint : this.target.getBoundingBox().getCenter());
            double distToActual = mc.player.getEyePos().distanceTo(this.target.getBoundingBox().getCenter());
            if (distToPredicted > maxDist && distToActual > maxDist) return false;
        } else if ((!mc.player.isGliding() || !this.target.isGliding())
                && mc.player.getEyePos().distanceTo(MultipointUtils.getNearestPoint(this.target, this.distance.getCurrent())) > this.distance.getCurrent()) {
            return false;
        }

        if (this.raycastCheck.isEnabled()) {
            return RaytracingUtil.rayTrace(mc.player.getRotationVector(), this.distance.getCurrent(), this.target.getBoundingBox()) || mc.targetedEntity != null;
        }
        return true;
    }

    private LivingEntity updateTarget() {
        List<LivingEntity> targets = new ArrayList<>();

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player instanceof LivingEntity living && this.isValid(living)) targets.add(living);
        }

        try {
            for (Entity entity : mc.world.getEntities()) {
                if (entity instanceof LivingEntity living && !(entity instanceof PlayerEntity)) {
                    if (this.isValid(living)) targets.add(living);
                }
            }
        } catch (Exception ignored) {}

        if (!targets.isEmpty() && this.isEnabled()) {
            targets.sort(Comparator.comparingDouble(entityx -> {
                double score = 0;
                if (targetPriority.isEnable("Здоровье")) score += ((LivingEntity) entityx).getHealth();
                if (targetPriority.isEnable("Дистанция")) score += mc.player.squaredDistanceTo(entityx) * 0.1;
                if (targetPriority.isEnable("Зрение")) {
                    Rotation vec = Rotation.getRotations(entityx.getBoundingBox().getCenter());
                    double dy = Math.abs(MathHelper.wrapDegrees(vec.getYaw() - mc.player.getYaw()));
                    double dp = Math.abs(MathHelper.wrapDegrees(vec.getPitch() - mc.player.getPitch()));
                    score += (dy + dp) * 0.5;
                }
                return score;
            }));
            return targets.get(0);
        }
        return null;
    }

    public boolean isValid(LivingEntity entity) {
        if (entity == mc.player) return false;
        if (!entity.isAlive() || entity.getHealth() <= 0.0F) return false;
        if (!mc.player.isAlive() || mc.player.getHealth() <= 0.0F) return false;
        if (skipInvisible.isEnabled() && entity.isInvisible() && isFullyInvisible(entity)) return false;

        if (entity instanceof PlayerEntity player) {
            if (!this.targetTypeSetting.isEnable("Игроков")) return false;
            if (Quilt.getInstance().getFriendManager().isFriend(entity.getName().getString())) return false;
            if (AntiBot.INSTANCE.isBot(player)) return false;
        }

        if ((entity instanceof PassiveEntity || entity instanceof FishEntity)
                && (!this.targetTypeSetting.isEnable("Животных") || Quilt.getInstance().getServerHandler().isPvp())) return false;

        if ((entity instanceof HostileEntity || entity instanceof AmbientEntity)
                && (!this.targetTypeSetting.isEnable("Мобов") || Quilt.getInstance().getServerHandler().isPvp())) return false;

        if (mc.player.getEyePos().distanceTo(MultipointUtils.getNearestPoint(entity,
                (double) (this.distance.getCurrent() + this.distanceRotation.getCurrent())))
                > (double) (mc.player.isGliding() ? 20.0F : this.distance.getCurrent() + this.distanceRotation.getCurrent())) {
            return false;
        }

        return !(entity instanceof ArmorStandEntity);
    }

    private boolean shouldPrepareSprintReset() {
        if (!this.sprintReset.isEnabled() || !mc.player.isSprinting()) return false;
        if (this.sprintResetDone) return this.sprintResetTicks < 1;
        this.needSprintReset = true;
        return true;
    }

    private boolean shouldSkipSprintResetInWater() {
        return mc.player != null && (mc.player.isTouchingWater() || mc.player.isSubmergedInWater());
    }

    private void resetSprintResetState() {
        this.needSprintReset = false;
        this.sprintResetDone = false;
        this.sprintResetTicks = 0;
    }

    public LivingEntity getTarget() {
        return this.isEnabled() ? this.target : null;
    }

    @Override
    public void onEnable() {
        this.target = null;
        this.lastPredictedPoint = null;
        this.smoothedAimPoint = null;
        this.smoothedTargetVelocity = Vec3d.ZERO;
        this.visualLookInitialized = false;
        this.resetSprintResetState();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        Quilt.getInstance().getModuleManager().setAcceleration(0.0F);
        this.resetElytraPredictState();
        this.resetSprintResetState();
        this.rotHuman.resetHistory();
        super.onDisable();
    }
}
