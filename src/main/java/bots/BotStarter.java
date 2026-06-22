package bots;

import bots.connection.BotNetwork;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.packet.c2s.handshake.ConnectionIntent;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.util.UUID;

/**
 * Starts bot connections to Minecraft servers
 */
public class BotStarter {
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * Run a bot with default port (25565)
     * @param botName The name of the bot
     * @param ip The server IP to connect to
     */
    public static void run(String botName, String ip) {
        run(botName, ip, 25565);
    }
    
    /**
     * Run a bot with custom port
     * @param botName The name of the bot
     * @param ip The server IP to connect to
     * @param port The server port
     */
    public static void run(String botName, String ip, int port) {
        new Thread(() -> {
            try {
                GameProfile gameProfile = new GameProfile(null, botName);
                BotNetwork botNetwork = BotNetwork.createNetworkManagerAndConnect(InetAddress.getByName(ip), port, false);
                BotManager.pendingNetworks.add(botNetwork);
                
                // Set up login handler
                // botNetwork.setNetHandler(new BotClientLoginNetHandler(botNetwork, null, null, (status) -> {}, botName));
                
                // Send handshake and login packets (placeholders for now)
                // HandshakeC2SPacket requires: protocolVersion, address, port, intendedState
                // For 1.21.4, we need to figure out the correct protocol version
                botNetwork.sendPacket(new HandshakeC2SPacket(765, ip, port, ConnectionIntent.LOGIN));
                Thread.sleep(500L);
                botNetwork.sendPacket(new LoginHelloC2SPacket(botName, UUID.randomUUID()));
                
                // Create bot and add to manager
                Bot bot = new Bot(botName, ip, port);
                bot.setNetworkManager(botNetwork);
                BotManager.allBots.add(bot);
                bot.setConnected(true);
                
                LOGGER.info("Bot '{}' started and connected to {}:{}", botName, ip, port);
            } catch (Exception e) {
                LOGGER.error("Failed to start bot '{}': {}", botName, e.getMessage(), e);
            }
        }).start();
    }
    
    /**
     * Run multiple bots
     * @param botNames Names of bots to start
     * @param ip Server IP
     * @param port Server port
     */
    public static void runMultiple(String[] botNames, String ip, int port) {
        for (String name : botNames) {
            run(name, ip, port);
        }
    }
    
    /**
     * Stop a specific bot
     * @param botName Name of the bot to stop
     */
    public static void stop(String botName) {
        Bot bot = BotManager.getBotByName(botName);
        if (bot != null) {
            bot.setConnected(false);
            if (bot.getNetworkManager() != null) {
                bot.getNetworkManager().disconnect();
            }
            BotManager.allBots.remove(bot);
            LOGGER.info("Stopping bot: {}", botName);
        } else {
            LOGGER.warn("Bot '{}' not found", botName);
        }
    }
    
    /**
     * Stop all bots
     */
    public static void stopAll() {
        LOGGER.info("Stopping all bots...");
        for (Bot bot : BotManager.allBots) {
            bot.setConnected(false);
            if (bot.getNetworkManager() != null) {
                bot.getNetworkManager().disconnect();
            }
        }
        BotManager.clearAll();
    }
}
