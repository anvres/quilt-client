package bots;

import bots.connection.BotNetwork;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
        LOGGER.info("Starting bot '{}' connecting to {}:{}", botName, ip, port);
        
        // Create bot instance
        Bot bot = new Bot(botName, ip, port);
        BotManager.addBot(bot);
        
        // Create network connection
        BotNetwork network = new BotNetwork(ip, port, botName);
        BotManager.addPendingNetwork(network);
        
        // Start connection in background thread
        new Thread(() -> {
            try {
                network.connect();
                bot.setConnected(true);
                
                // Wait for disconnection
                while (bot.isConnected()) {
                    Thread.sleep(1000);
                }
                
                BotManager.removeBot(bot);
                BotManager.removePendingNetwork(network);
                LOGGER.info("Bot '{}' disconnected", botName);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.error("Bot thread interrupted for {}", botName);
            } catch (Exception e) {
                LOGGER.error("Failed to start bot '{}': {}", botName, e.getMessage(), e);
                BotManager.removeBot(bot);
                BotManager.removePendingNetwork(network);
            }
        }, "BotThread-" + botName).start();
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
        for (Bot bot : BotManager.getAllBots()) {
            bot.setConnected(false);
        }
        BotManager.clearAll();
    }
}
