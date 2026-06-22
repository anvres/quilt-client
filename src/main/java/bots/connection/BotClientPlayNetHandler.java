package bots.connection;

/**
 * Stub for bot client play network handler
 * This is a placeholder - actual packet handling to be added
 */
public class BotClientPlayNetHandler {
    private BotNetwork networkManager;
    private String botName;
    
    public BotClientPlayNetHandler(BotNetwork networkManager, String botName) {
        this.networkManager = networkManager;
        this.botName = botName;
    }
    
    public BotNetwork getNetworkManager() {
        return networkManager;
    }
    
    public void setNetworkManager(BotNetwork networkManager) {
        this.networkManager = networkManager;
    }
    
    public String getBotName() {
        return botName;
    }
    
    public void setBotName(String botName) {
        this.botName = botName;
    }
}
