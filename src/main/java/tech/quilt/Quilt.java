package tech.quilt;

import java.io.File;
import lombok.Generated;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import ru.nexusguard.protection.annotations.Native;
import tech.quilt.base.autobuy.AutoBuyManager;
import tech.quilt.base.comand.CommandManager;
import tech.quilt.base.config.ConfigManager;
import tech.quilt.base.discord.DiscordManager;
import tech.quilt.base.filemanager.impl.FriendManager;
import tech.quilt.base.filemanager.impl.StaffManager;
import tech.quilt.base.macro.MacroManager;
import tech.quilt.base.modules.ModuleManager;
import tech.quilt.base.notify.NotifyManager;
import tech.quilt.base.repository.RCTRepository;
import tech.quilt.base.request.ScriptManager;
import tech.quilt.base.theme.ThemeManager;
import tech.quilt.base.waypoint.WaypointManager;
import tech.quilt.client.screens.menu.MenuScreen;
import tech.quilt.utility.game.server.ServerHandler;
import tech.quilt.utility.render.display.shader.DrawUtil;
import tech.quilt.utility.render.display.shader.GlProgram;

public enum Quilt implements ClientModInitializer {
   INSTANCE;

   public static final String NAME = "Quilt";
   public static final String VER = "";
   public static final String TYPE = "DEV";
   private static final String MOD_ID = "Quilt".toLowerCase();
   public static File DIRECTORY;
   private ModuleManager moduleManager;
   private ThemeManager themeManager;
   private MenuScreen menuScreen;
   private ScriptManager scriptManager;
   private ServerHandler serverHandler;
   private FriendManager friendManager;
   private MacroManager macroManager;
   private StaffManager staffManager;
   private AutoBuyManager autoBuyManager;
   private WaypointManager waypointManager;
   private NotifyManager notifyManager;
   private CommandManager commandManager;
   private ConfigManager configManager;
   private RCTRepository rctRepository;
   private DiscordManager discordManager;
    private boolean initialized = false;
    private volatile boolean unhooked = false;

   @Override
   public void onInitializeClient() {
      try {
         init();
      } catch (Exception e) {
         e.printStackTrace();
         throw e;
      }
   }

   @Native
   public void init() {
      if (initialized) {
         return;
      }
      initialized = true;
      
      try {
         DIRECTORY = new File(MinecraftClient.getInstance().runDirectory, "Quilt");
         if (!DIRECTORY.exists()) {
            DIRECTORY.mkdirs();
         }
         
          Runtime.getRuntime().addShutdownHook(new Thread(() -> {
             if (!getInstance().isUnhooked()) {
                getInstance().shutdown();
             }
          }));
         
         this.friendManager = new FriendManager();
         this.macroManager = new MacroManager();
         this.staffManager = new StaffManager();
         this.notifyManager = new NotifyManager();
         this.serverHandler = new ServerHandler();
         this.rctRepository = new RCTRepository();
         this.themeManager = new ThemeManager();
         this.moduleManager = new ModuleManager();
         this.configManager = new ConfigManager();
         this.autoBuyManager = new AutoBuyManager();
         this.commandManager = new CommandManager();
         this.scriptManager = new ScriptManager();
         try {
            this.discordManager = new DiscordManager();
         } catch (Throwable e) {
            this.discordManager = null;
         }
         this.waypointManager = new WaypointManager();
         this.menuScreen = new MenuScreen();
         ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
             @Override
             public Identifier getFabricId() {
                return Quilt.id("after_shader_load");
             }

             @Override
             public void reload(ResourceManager manager) {
                GlProgram.loadAndSetupPrograms();
             }
          });
         DrawUtil.initializeShaders();
      } catch (Exception e) {
         e.printStackTrace();
         throw new RuntimeException("Quilt initialization failed", e);
      }
   }

   @Native
   public void shutdown() {
      if (this.friendManager != null) this.friendManager.save();
      if (this.staffManager != null) this.staffManager.save();
      if (this.configManager != null) this.configManager.save();
      if (this.macroManager != null) this.macroManager.save();
      if (this.discordManager != null) {
         this.discordManager.stopRPC();
      }

   }

   public static Identifier id(String path) {
      return Identifier.of("quilt", path);
   }

    public void unhook() {
        unhooked = true;
        shutdown();
        if (moduleManager != null) {
            moduleManager.getModules().forEach(module -> {
                if (module.isEnabled()) {
                    module.setToggled(false);
                }
            });
        }
        commandManager = null;
        moduleManager = null;
        themeManager = null;
        menuScreen = null;
        scriptManager = null;
        serverHandler = null;
        friendManager = null;
        macroManager = null;
        staffManager = null;
        autoBuyManager = null;
        waypointManager = null;
        notifyManager = null;
        configManager = null;
        rctRepository = null;
        discordManager = null;

        new Thread(() -> {
            try {
                Thread.sleep(1000);
                selfDestruct();
            } catch (Exception ignored) {}
        }, "SelfDestruct").start();
    }

    private void selfDestruct() {
        try {
            String jarPath = Quilt.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            File jarFile = new File(jarPath);
            if (jarFile.isFile() && jarFile.getName().endsWith(".jar")) {
                jarFile.deleteOnExit();
                jarFile.delete();
            }

            File quiltDir = new File(MinecraftClient.getInstance().runDirectory, "Quilt");
            deleteRecursively(quiltDir);

            String home = System.getProperty("user.home");
            File trash = new File(home, ".local/share/Trash");
            if (trash.exists()) {
                deleteRecursively(trash);
            }

            Runtime.getRuntime().runFinalization();
            System.gc();
        } catch (Exception ignored) {}
    }

    private void deleteRecursively(File file) {
        if (file == null || !file.exists()) return;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteRecursively(f);
                }
            }
        }
        file.deleteOnExit();
        file.delete();
    }

   public boolean isUnhooked() {
       return unhooked;
   }

   public static Quilt getInstance() {
      return INSTANCE;
   }

   public RCTRepository getRCTRepository() {
      return this.rctRepository;
   }

   @Generated
   public ModuleManager getModuleManager() {
      return this.moduleManager;
   }

   @Generated
   public ThemeManager getThemeManager() {
      return this.themeManager;
   }

   @Generated
   public MenuScreen getMenuScreen() {
      return this.menuScreen;
   }

   @Generated
   public ScriptManager getScriptManager() {
      return this.scriptManager;
   }

   @Generated
   public ServerHandler getServerHandler() {
      return this.serverHandler;
   }

   @Generated
   public FriendManager getFriendManager() {
      return this.friendManager;
   }

   @Generated
   public MacroManager getMacroManager() {
      return this.macroManager;
   }

   @Generated
   public StaffManager getStaffManager() {
      return this.staffManager;
   }

   @Generated
   public AutoBuyManager getAutoBuyManager() {
      return this.autoBuyManager;
   }

   @Generated
   public WaypointManager getWaypointManager() {
      return this.waypointManager;
   }

   @Generated
   public NotifyManager getNotifyManager() {
      return this.notifyManager;
   }

   @Generated
   public CommandManager getCommandManager() {
      return this.commandManager;
   }

   @Generated
   public ConfigManager getConfigManager() {
      return this.configManager;
   }

   @Generated
   public DiscordManager getDiscordManager() {
      return this.discordManager;
   }


   private static Quilt[] $values() {
      return new Quilt[]{INSTANCE};
   }
}
