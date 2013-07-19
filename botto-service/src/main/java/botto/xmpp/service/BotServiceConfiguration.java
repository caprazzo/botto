package botto.xmpp.service;

public class BotServiceConfiguration {
    private int componentPort;
    private int clientPort;
    private String host;

    private String secret;


    public String getHost() {
        return host;
    }

    public int getComponentPort() {
        return componentPort;
    }

    public String getComponentSecret(String subdomain) {
        return secret;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setComponentPort(int port) {
        this.componentPort = port;
    }

    public void setComponentSecret(String secret) {
        this.secret = secret;
    }

    public int getClientPort() {
        return clientPort;
    }

    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }
}
