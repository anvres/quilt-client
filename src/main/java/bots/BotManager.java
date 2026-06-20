package bots;

import bots.connection.BotNetwork;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages all bot instances
 */
public class BotManager {
    private static final Logger LOGGER = LogManager.getLogger();
    
    private static final List<Bot> allBots = new CopyOnWriteArrayList<>();
    private static final List<BotNetwork> pendingNetworks = new CopyOnWriteArrayList<>();
    
    public static List<Bot> getAllBots() {
        return new ArrayList<>(allBots);
    }
    
    public static List<BotNetwork> getPendingNetworks() {
        return new ArrayList<>(pendingNetworks);
    }
    
    public static void addBot(Bot bot) {
        allBots.add(bot);
        LOGGER.info("Added bot: {}", bot.getName());
    }
    
    public static void removeBot(Bot bot) {
        allBots.remove(bot);
        LOGGER.info("Removed bot: {}", bot.getName());
    }
    
    public static void addPendingNetwork(BotNetwork network) {
        pendingNetworks.add(network);
        LOGGER.debug("Added pending network for bot: {}", network.getBotName());
    }
    
    public static void removePendingNetwork(BotNetwork network) {
        pendingNetworks.remove(network);
        LOGGER.debug("Removed pending network for bot: {}", network.getBotName());
    }
    
    public static int getBotCount() {
        return allBots.size();
    }
    
    public static int getPendingCount() {
        return pendingNetworks.size();
    }
    
    public static void clearAll() {
        for (Bot bot : allBots) {
            // TODO: Disconnect bot properly
        }
        allBots.clear();
        pendingNetworks.clear();
        LOGGER.info("Cleared all bots and pending networks");
    }
    
    public static Bot getBotByName(String name) {
        for (Bot bot : allBots) {
            if (bot.getName().equalsIgnoreCase(name)) {
                return bot;
            }
        }
        return null;
    }
}
