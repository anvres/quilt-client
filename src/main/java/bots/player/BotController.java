package bots.player;

/**
 * Stub for bot controller
 * This is a placeholder - actual control logic to be added
 */
public class BotController {
    private BotPlayer botPlayer;
    
    public BotController(BotPlayer botPlayer) {
        this.botPlayer = botPlayer;
    }
    
    public BotPlayer getBotPlayer() {
        return botPlayer;
    }
    
    public void setBotPlayer(BotPlayer botPlayer) {
        this.botPlayer = botPlayer;
    }
}
