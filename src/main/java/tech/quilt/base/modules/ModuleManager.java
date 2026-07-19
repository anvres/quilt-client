package tech.quilt.base.modules;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import lombok.Generated;
import net.minecraft.client.option.Perspective;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.util.math.MathHelper;
import tech.quilt.Quilt;
import tech.quilt.base.events.impl.input.EventKey;
import tech.quilt.base.events.impl.other.EventGameUpdate;
import tech.quilt.base.events.impl.render.EventHudRender;
import tech.quilt.base.events.impl.server.EventPacket;
import tech.quilt.base.macro.Macro;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.setting.Setting;
import tech.quilt.client.modules.api.setting.impl.BooleanSetting;
import tech.quilt.client.modules.api.setting.impl.ModeSetting;
import tech.quilt.client.modules.api.setting.impl.MultiBooleanSetting;
import tech.quilt.client.modules.api.setting.impl.ModeSetting.Value;
import tech.quilt.client.modules.impl.combat.AntiBot;
import tech.quilt.client.modules.impl.combat.AntiFire;
import tech.quilt.client.modules.impl.combat.AntiKnockback;
import tech.quilt.client.modules.impl.combat.Aura;
import tech.quilt.client.modules.impl.combat.Aura18;
import tech.quilt.client.modules.impl.combat.AutoEat;
import tech.quilt.client.modules.impl.combat.AutoGapple;
import tech.quilt.client.modules.impl.combat.AutoHead;
import tech.quilt.client.modules.impl.combat.AutoRod;
import tech.quilt.client.modules.impl.combat.BlockHit;
import tech.quilt.client.modules.impl.combat.FastBow;
import tech.quilt.client.modules.impl.combat.FastPlace;
import tech.quilt.client.modules.impl.combat.KeepSprint;
import tech.quilt.client.modules.impl.combat.NoClickDelay;
import tech.quilt.client.modules.impl.combat.NoFall;
import tech.quilt.client.modules.impl.combat.SafeWalk;
import tech.quilt.client.modules.impl.combat.Scaffold;
import tech.quilt.client.modules.impl.combat.WTap;
import tech.quilt.client.modules.impl.combat.AutoCrystal;
import tech.quilt.client.modules.impl.combat.AutoSwap;
import tech.quilt.client.modules.impl.combat.AutoTotem;
import tech.quilt.client.modules.impl.combat.ClickPearl;
import tech.quilt.client.modules.impl.combat.Criticals;
import tech.quilt.client.modules.impl.combat.HitBox;
import tech.quilt.client.modules.impl.combat.PacketCriticals;
import tech.quilt.client.modules.impl.combat.SuperBow;
import tech.quilt.client.modules.impl.combat.TriggerBot;
import tech.quilt.client.modules.impl.combat.Velocity;
import tech.quilt.client.modules.impl.misc.AHHelper;
import tech.quilt.client.modules.impl.misc.AnyDeskMonitor;
import tech.quilt.client.modules.impl.misc.AutoAccept;
import tech.quilt.client.modules.impl.misc.AutoDuel;
import tech.quilt.client.modules.impl.misc.AutoRespawn;
import tech.quilt.client.modules.impl.misc.FTHelper;
import tech.quilt.client.modules.impl.misc.GriefJoiner;
import tech.quilt.client.modules.impl.misc.HWHelper;
import tech.quilt.client.modules.impl.misc.HealthResolver;
import tech.quilt.client.modules.impl.misc.ItemScroller;
import tech.quilt.client.modules.impl.misc.Joiner;
import tech.quilt.client.modules.impl.misc.ClickAction;
import tech.quilt.client.modules.impl.misc.ElytraHelper;
import tech.quilt.client.modules.impl.misc.FreeCam;
import tech.quilt.client.modules.impl.misc.NameProtect;
import tech.quilt.client.modules.impl.misc.NoInteract;
import tech.quilt.client.modules.impl.misc.ScoreboardHealth;
import tech.quilt.client.modules.impl.misc.ServerCrasher;
import tech.quilt.client.modules.impl.misc.ServerHelper;
import tech.quilt.client.modules.impl.misc.TapeMouse;
import tech.quilt.client.modules.impl.movement.AirStuck;
import tech.quilt.client.modules.impl.movement.AutoSprint;
import tech.quilt.client.modules.impl.movement.ElytraResolver;
import tech.quilt.client.modules.impl.movement.Timer;
import tech.quilt.client.modules.impl.movement.ElytraSample;
import tech.quilt.client.modules.impl.movement.ElytraTarget;
import tech.quilt.client.modules.impl.movement.GrimGlide;
import tech.quilt.client.modules.impl.movement.ElytraBooster;
import tech.quilt.client.modules.impl.movement.ElytraControl;
import tech.quilt.client.modules.impl.movement.ElytraExploit;
import tech.quilt.client.modules.impl.movement.ElytraManager;
import tech.quilt.client.modules.impl.movement.Flight;
import tech.quilt.client.modules.impl.movement.ElytraMotion;
import tech.quilt.client.modules.impl.movement.ElytraRecast;
import tech.quilt.client.modules.impl.movement.GuiWalk;
import tech.quilt.client.modules.impl.movement.Jesus;
import tech.quilt.client.modules.impl.movement.NoSlow;
import tech.quilt.client.modules.impl.movement.NoWeb;
import tech.quilt.client.modules.impl.movement.RWSpeed;
import tech.quilt.client.modules.impl.movement.Speed;
import tech.quilt.client.modules.impl.movement.Strafe;
import tech.quilt.client.modules.impl.movement.SuperFirework;
import tech.quilt.client.modules.impl.movement.VanillaSpeed;
import tech.quilt.client.modules.impl.player.AutoArmor;
import tech.quilt.client.modules.impl.player.AutoMessage;
import tech.quilt.client.modules.impl.player.Assistant;
import tech.quilt.client.modules.impl.player.AutoLeave;
import tech.quilt.client.modules.impl.player.FakeLag;
import tech.quilt.client.modules.impl.player.AutoTool;
import tech.quilt.client.modules.impl.player.Blink;
import tech.quilt.client.modules.impl.player.ChestStealer;
import tech.quilt.client.modules.impl.player.FastBreak;
import tech.quilt.client.modules.impl.player.NoDelay;
import tech.quilt.client.modules.impl.player.NoPush;
import tech.quilt.client.modules.impl.render.AntiInvisible;
import tech.quilt.client.modules.impl.render.Arrows;
import tech.quilt.client.modules.impl.render.SeeInvisibles;
import tech.quilt.client.modules.impl.render.Crosshair;
import tech.quilt.client.modules.impl.render.CustomFog;
import tech.quilt.client.modules.impl.render.EntityESP;
import tech.quilt.client.modules.impl.render.ExtraTab;
import tech.quilt.client.modules.impl.render.FullBright;
import tech.quilt.client.modules.impl.render.Interface;
import tech.quilt.client.modules.impl.render.Menu;
import tech.quilt.client.modules.impl.render.Menu2Module;
import tech.quilt.client.modules.impl.render.NoRender;
import tech.quilt.client.modules.impl.render.Predictions;
import tech.quilt.client.modules.impl.render.SwingAnimation;
import tech.quilt.client.modules.impl.render.TargetESP;
import tech.quilt.client.modules.impl.render.ViewModel;
import tech.quilt.client.modules.impl.render.WorldTime;
import tech.quilt.client.modules.impl.render.CameraClip;
import tech.quilt.client.modules.impl.render.CustomModels;
import tech.quilt.client.modules.impl.render.Cubes;
import tech.quilt.client.modules.impl.render.RichiDog;
import tech.quilt.client.modules.impl.render.Hands;
import tech.quilt.client.screens.menu.MenuScreen;
import tech.quilt.utility.component.RotationComponent;
import tech.quilt.utility.game.player.rotation.Rotation;
import tech.quilt.utility.interfaces.IMinecraft;

public final class ModuleManager implements IMinecraft {
   private final List<Module> modules = new ArrayList<>();
   private boolean isBack;
   private boolean isRotated;
   private float acceleration;

   private long lastKeyPressTime = 0;
   private int lastKeyCode = -1;
   private static final long DEBOUNCE_THRESHOLD_MS = 200;
   private long lastAnimationUpdateTime = 0;
   private static final long ANIMATION_UPDATE_INTERVAL_MS = 16;

   public ModuleManager() {
      init();
      EventManager.register(this);
   }

   private void init() {
      registerCombat();
      registerMovement();
      registerRender();
      registerPlayer();
      registerMisc();
   }

    private void registerCombat() {
       registerModule(AntiBot.INSTANCE);
       registerModule(Aura.INSTANCE);
       registerModule(Aura18.INSTANCE);
       registerModule(AutoHead.INSTANCE);
       registerModule(BlockHit.INSTANCE);
       registerModule(KeepSprint.INSTANCE);
       registerModule(NoClickDelay.INSTANCE);
       registerModule(WTap.INSTANCE);
       registerModule(AntiKnockback.INSTANCE);
       registerModule(AntiFire.INSTANCE);
       registerModule(AutoEat.INSTANCE);
       registerModule(AutoGapple.INSTANCE);
       registerModule(AutoRod.INSTANCE);
       registerModule(FastBow.INSTANCE);
       registerModule(FastPlace.INSTANCE);
       registerModule(NoFall.INSTANCE);
       registerModule(SafeWalk.INSTANCE);
       registerModule(Scaffold.INSTANCE);
       registerModule(AutoCrystal.INSTANCE);
       registerModule(AutoSwap.INSTANCE);
       registerModule(AutoTotem.INSTANCE);
        registerModule(ClickPearl.INSTANCE);
        registerModule(Criticals.INSTANCE);
        registerModule(HitBox.INSTANCE);
        registerModule(PacketCriticals.INSTANCE);
        registerModule(SuperBow.INSTANCE);
        registerModule(TriggerBot.INSTANCE);
       registerModule(Velocity.INSTANCE);
    }

    private void registerMovement() {
       registerModule(AutoSprint.INSTANCE);
       registerModule(ElytraBooster.INSTANCE);
       registerModule(ElytraControl.INSTANCE);
       registerModule(ElytraManager.INSTANCE);
       registerModule(ElytraRecast.INSTANCE);
       registerModule(ElytraExploit.INSTANCE);
        registerModule(GrimGlide.INSTANCE);
        registerModule(GuiWalk.INSTANCE);
        registerModule(Jesus.INSTANCE);
        registerModule(NoSlow.INSTANCE);
        registerModule(RWSpeed.INSTANCE);
        registerModule(Speed.INSTANCE);
        registerModule(AirStuck.INSTANCE);
        registerModule(ElytraMotion.INSTANCE);
        registerModule(ElytraResolver.INSTANCE);
        registerModule(ElytraTarget.INSTANCE);
        registerModule(NoWeb.INSTANCE);
        registerModule(SuperFirework.INSTANCE);
        registerModule(ElytraSample.INSTANCE);
        registerModule(Strafe.INSTANCE);
        registerModule(Timer.INSTANCE);
        registerModule(VanillaSpeed.INSTANCE);
    }

    private void registerRender() {
       registerModule(Interface.INSTANCE);
       registerModule(AntiInvisible.INSTANCE);
       registerModule(Menu.INSTANCE);
       registerModule(NoRender.INSTANCE);
       registerModule(Predictions.INSTANCE);
       registerModule(SwingAnimation.INSTANCE);
       registerModule(Crosshair.INSTANCE);
       registerModule(ViewModel.INSTANCE);
       registerModule(CustomFog.INSTANCE);
       registerModule(FullBright.INSTANCE);
       registerModule(WorldTime.INSTANCE);
       registerModule(EntityESP.INSTANCE);
       registerModule(TargetESP.INSTANCE);
       registerModule(Arrows.INSTANCE);
       registerModule(ExtraTab.INSTANCE);
       registerModule(SeeInvisibles.INSTANCE);
        registerModule(CameraClip.INSTANCE);
        registerModule(Menu2Module.INSTANCE);
        registerModule(CustomModels.INSTANCE);
        registerModule(Cubes.INSTANCE);
        registerModule(RichiDog.INSTANCE);
        registerModule(Hands.INSTANCE);
    }

    private void registerPlayer() {
       registerModule(AutoTool.INSTANCE);
       registerModule(AutoArmor.INSTANCE);
       registerModule(AutoLeave.INSTANCE);
       registerModule(Assistant.INSTANCE);
       registerModule(Blink.INSTANCE);
       registerModule(FakeLag.INSTANCE);
       registerModule(NoDelay.INSTANCE);
       registerModule(FastBreak.INSTANCE);
       registerModule(NoPush.INSTANCE);
       registerModule(ChestStealer.INSTANCE);
       registerModule(AutoMessage.INSTANCE);
    }

    private void registerMisc() {
       registerModule(ServerHelper.INSTANCE);
       registerModule(ElytraHelper.INSTANCE);
       registerModule(ItemScroller.INSTANCE);
       registerModule(ClickAction.INSTANCE);
       registerModule(FreeCam.INSTANCE);
       registerModule(AHHelper.INSTANCE);
       registerModule(HealthResolver.INSTANCE);
       registerModule(Joiner.INSTANCE);
       registerModule(TapeMouse.INSTANCE);
       registerModule(NoInteract.INSTANCE);
       registerModule(AutoAccept.INSTANCE);
       registerModule(AutoDuel.INSTANCE);
       registerModule(AutoRespawn.INSTANCE);
       registerModule(NameProtect.INSTANCE);
       registerModule(FTHelper.INSTANCE);
         registerModule(ScoreboardHealth.INSTANCE);
         registerModule(ServerCrasher.INSTANCE);
          registerModule(AnyDeskMonitor.INSTANCE);
         registerModule(GriefJoiner.INSTANCE);
         registerModule(Flight.INSTANCE);
         registerModule(HWHelper.INSTANCE);
    }

   private void registerModule(Module module) {
      modules.add(module);
   }

   public Module getModule(String name) {
      return modules.stream()
              .filter(module -> module.getName().equalsIgnoreCase(name))
              .findFirst()
              .orElse(null);
   }

   public Set<Module> getActiveModules() {
      Set<Module> active = new HashSet<>();
      for (Module module : modules) {
         if (module.isEnabled()) {
            active.add(module);
         }
      }
      return active;
   }

    @EventTarget
    public void onKey(EventKey event) {
       if (Quilt.getInstance().isUnhooked()) return;
       if (mc.currentScreen == null && event.getAction() == 1) {
         int keyCode = event.getKeyCode();
         long currentTime = System.currentTimeMillis();
         
         if (keyCode == lastKeyCode && (currentTime - lastKeyPressTime) < DEBOUNCE_THRESHOLD_MS) {
            return;
         }
         
         lastKeyCode = keyCode;
         lastKeyPressTime = currentTime;
         
         // Поиск модуля по keyCode
         for (Module module : modules) {
            if (module.getKeyCode() == keyCode) {
               module.toggle();
               break;
            }
         }

         for (Macro macro : Quilt.getInstance().getMacroManager().getItems()) {
            if (keyCode == macro.getBind()) {
               mc.getNetworkHandler().sendChatMessage(macro.getText());
            }
         }
      }
   }

   @EventTarget
   public void onRender(EventHudRender e) {
      long currentTime = System.currentTimeMillis();
      
      // Обновляем анимации темы всегда
      Quilt.getInstance().getThemeManager().getCurrentTheme().getAnimation().update(1.0F, currentTime);
      
      // Обновляем анимации модулей не чаще 60 FPS (каждые 16ms) для оптимизации производительности
      if (currentTime - lastAnimationUpdateTime >= ANIMATION_UPDATE_INTERVAL_MS) {
         lastAnimationUpdateTime = currentTime;
         
         for (Module module : modules) {
            if (module.isEnabled()) {
               module.getAnimation().update(true, currentTime);

               for (Setting setting : module.getSettings()) {
                  if (setting instanceof BooleanSetting booleanSetting) {
                     booleanSetting.getAnimation().update(booleanSetting.isEnabled(), currentTime);
                  } else if (setting instanceof ModeSetting modeSetting) {
                     for (ModeSetting.Value value : modeSetting.getValues()) {
                        value.getAnimation().update(value.isSelected(), currentTime);
                     }
                  } else if (setting instanceof MultiBooleanSetting multiBooleanSetting) {
                     for (MultiBooleanSetting.Value value : multiBooleanSetting.getBooleanSettings()) {
                        value.getAnimation().update(value.isEnabled(), currentTime);
                     }
                  }
               }
            } else {
               module.getAnimation().update(false, currentTime);
            }
         }
      }

      MenuScreen menuScreen = Quilt.getInstance().getMenuScreen();
      if (menuScreen.needToClose) {
         if (menuScreen.savedRunnable != null) {
            menuScreen.savedRunnable.run();
         }

         if (menuScreen.openAnimationMetanoise.getValue() <= 0.27F) {
            menuScreen.savedRunnable = null;
            menuScreen.needToClose = false;
            menuScreen.openAnimationMetanoise.setValue(0.0F);
            menuScreen.openAnimationMetanoise.setStartValue(0.0F);
         }
      }
   }

   @EventTarget
   private void onPacket(EventPacket e) {
      if (e.getPacket() instanceof CloseScreenS2CPacket && mc.currentScreen instanceof MenuScreen) {
         e.cancel();
      }
   }

   @EventTarget
   private void onGameUpdate(EventGameUpdate e) {
      if (mc.player == null) return;

      if (!Aura.INSTANCE.isEnabled() || Aura.INSTANCE.getTarget() == null) {
         float cameraYaw = mc.gameRenderer.getCamera().getYaw();
         float cameraPitch = mc.gameRenderer.getCamera().getPitch();

         if (mc.options.getPerspective() == Perspective.THIRD_PERSON_FRONT) {
            Aura.INSTANCE.lastYaw = cameraYaw - 180.0F;
            Aura.INSTANCE.lastPitch = -cameraPitch;
         } else {
            Aura.INSTANCE.lastYaw = cameraYaw;
            Aura.INSTANCE.lastPitch = cameraPitch;
         }

         if (Aura.INSTANCE.rotationMode.is("Vanilla")) {
            return;
         }

         Rotation current = new Rotation(mc.player.getYaw(), mc.player.getPitch());
         float deltaYaw = MathHelper.wrapDegrees(cameraYaw - current.getYaw());
         float deltaPitch = cameraPitch - current.getPitch();

         if (mc.options.getPerspective() == Perspective.THIRD_PERSON_FRONT) {
            deltaYaw = MathHelper.wrapDegrees(cameraYaw - 180.0F - current.getYaw());
            deltaPitch = -cameraPitch - current.getPitch();
         }

         acceleration += 0.0024F;
         float smooth = MathHelper.clamp(acceleration, 0.0F, 1.0F);
         float newYaw = current.getYaw() + deltaYaw * smooth;
         float newPitch = current.getPitch() + deltaPitch * (smooth / 2.0F);

         Rotation smoothRot = new Rotation(newYaw, newPitch);
         RotationComponent.update(smoothRot, 360.0F, 360.0F, 360.0F, 360.0F, 0, 2, false);
      }
   }


   public List<Module> getModules() {
      return modules;
   }

   public boolean isBack() {
      return isBack;
   }

   public boolean isRotated() {
      return isRotated;
   }

   public float getAcceleration() {
      return acceleration;
   }

   public void setBack(boolean isBack) {
      this.isBack = isBack;
   }

   public void setRotated(boolean isRotated) {
      this.isRotated = isRotated;
   }

   public void setAcceleration(float acceleration) {
      this.acceleration = acceleration;
   }
}