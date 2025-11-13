package it.universita.server;

public class ServerConfig {
    private final int porta;
    private final String host;

    public ServerConfig(int porta, String host) {
        this.porta = porta;
        this.host = host;
    }

    public int getPorta() {
        return porta;
    }

    public String getHost() {
        return host;
    }


}
