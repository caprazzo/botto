package botto.xmpp.connectors.whack;

import botto.xmpp.botto.xmpp.connector.ConnectorConfiguration;

public class WhackConnectorConfiguration implements ConnectorConfiguration {

    private int port;
    private String host;
    private String domain;
    private String name;
    private String secret;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getSecret(String subdomain) {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
