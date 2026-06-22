package bots;

import bots.connection.BotNetwork;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages all bot instances
 */
public class BotManager {
    public static final List<Bot> allBots = new CopyOnWriteArrayList<>();
    public static final List<BotNetwork> pendingNetworks = new CopyOnWriteArrayList<>();

    public static List<Bot> getAllBots() {
        return allBots;
    }
    
    public static List<BotNetwork> getPendingNetworks() {
        return pendingNetworks;
    }
    
    public static void addBot(Bot bot) {
        allBots.add(bot);
    }
    
    public static void removeBot(Bot bot) {
        allBots.remove(bot);
    }
    
    public static void addPendingNetwork(BotNetwork network) {
        pendingNetworks.add(network);
    }
    
    public static void removePendingNetwork(BotNetwork network) {
        pendingNetworks.remove(network);
    }
    
    public static int getBotCount() {
        return allBots.size();
    }
    
    public static int getPendingCount() {
        return pendingNetworks.size();
    }
    
    public static void clearAll() {
        allBots.clear();
        pendingNetworks.clear();
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
