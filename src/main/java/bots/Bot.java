package bots;

import bots.connection.BotNetwork;
import bots.player.BotPlayer;
import bots.world.BotWorld;

/**
 * Represents a bot instance that can connect to a Minecraft server
 */
public class Bot {
    private String name;
    private String serverIp;
    private int serverPort;
    private BotNetwork networkManager;
    private BotWorld botWorld;
    private BotPlayer botPlayer;
    private boolean connected;
    private boolean codesCollected;
    private long lastTimeCollected = 0L;

    public Bot(String name) {
        this.name = name;
        this.connected = false;
    }

    public Bot(String name, String serverIp, int serverPort) {
        this(name);
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }

    public Bot(BotNetwork networkManager, BotWorld botWorld, BotPlayer botPlayer, String name) {
        this.name = name;
        this.networkManager = networkManager;
        this.botWorld = botWorld;
        this.botPlayer = botPlayer;
        this.connected = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public BotNetwork getNetworkManager() {
        return networkManager;
    }

    public void setNetworkManager(BotNetwork networkManager) {
        this.networkManager = networkManager;
    }

    public BotWorld getBotWorld() {
        return botWorld;
    }

    public void setBotWorld(BotWorld botWorld) {
        this.botWorld = botWorld;
    }

    public BotPlayer getBotPlayer() {
        return botPlayer;
    }

    public void setBotPlayer(BotPlayer botPlayer) {
        this.botPlayer = botPlayer;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isCodesCollected() {
        return codesCollected;
    }

    public void setCodesCollected(boolean codesCollected) {
        this.codesCollected = codesCollected;
    }

    public long getLastTimeCollected() {
        return lastTimeCollected;
    }

    public void setLastTimeCollected(long lastTimeCollected) {
        this.lastTimeCollected = lastTimeCollected;
    }

    @Override
    public String toString() {
        return "Bot{name='" + name + "', server=" + serverIp + ":" + serverPort + ", connected=" + connected + "}";
    }
}
