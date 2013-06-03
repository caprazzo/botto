package botto.xmpp.service;

public class BotServiceConfiguration {
    private String host;
    private int port;
    private String secret;

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getSecret(String subdomain) {
        return secret;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
