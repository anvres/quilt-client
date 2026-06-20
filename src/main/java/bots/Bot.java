package bots;

/**
 * Represents a bot instance
 */
public class Bot {
    private final String name;
    private String serverIp;
    private int serverPort;
    private boolean connected;
    
    public Bot(String name) {
        this.name = name;
        this.connected = false;
    }
    
    public Bot(String name, String serverIp, int serverPort) {
        this(name);
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }
    
    public String getName() {
        return name;
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
    
    public boolean isConnected() {
        return connected;
    }
    
    public void setConnected(boolean connected) {
        this.connected = connected;
    }
    
    @Override
    public String toString() {
        return "Bot{name='" + name + "', server=" + serverIp + ":" + serverPort + ", connected=" + connected + "}";
    }
}
