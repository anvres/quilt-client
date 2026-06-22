package bots.player;

import bots.connection.BotClientPlayNetHandler;
import bots.world.BotWorld;

/**
 * Simplified bot player for 1.21.4
 * This is a stub implementation - actual player logic to be added
 */
public class BotPlayer {
    private String name;
    private BotWorld botWorld;
    private BotClientPlayNetHandler connection;
    
    public BotPlayer(String name, BotWorld botWorld, BotClientPlayNetHandler connection) {
        this.name = name;
        this.botWorld = botWorld;
        this.connection = connection;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public BotWorld getBotWorld() {
        return botWorld;
    }
    
    public void setBotWorld(BotWorld botWorld) {
        this.botWorld = botWorld;
    }
    
    public BotClientPlayNetHandler getConnection() {
        return connection;
    }
    
    public void setConnection(BotClientPlayNetHandler connection) {
        this.connection = connection;
    }
}
