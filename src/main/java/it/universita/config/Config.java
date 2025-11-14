package it.universita.config;


public class Config {

    private final String serverHost;
    private final int serverPort;

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    // eventuale sezione client config
    private final String clientHost;
    private final int clientPort;

    public Config(String serverHost, int serverPort,
                  String dbUrl, String dbUser, String dbPassword,
                  String clientHost, int clientPort) {

        this.serverHost = serverHost;
        this.serverPort = serverPort;

        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;

        this.clientHost = clientHost;
        this.clientPort = clientPort;
    }

    // Getter
    public String getServerHost() { return serverHost; }
    public int getServerPort() { return serverPort; }

    public String getDbUrl() { return dbUrl; }
    public String getDbUser() { return dbUser; }
    public String getDbPassword() { return dbPassword; }

    public String getClientHost() { return clientHost; }
    public int getClientPort() { return clientPort; }
}

