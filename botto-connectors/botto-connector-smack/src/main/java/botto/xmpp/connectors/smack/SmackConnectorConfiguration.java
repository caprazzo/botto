package botto.xmpp.connectors.smack;

import botto.xmpp.botto.xmpp.connector.ConnectorConfiguration;
import org.xmpp.packet.JID;

public class SmackConnectorConfiguration implements ConnectorConfiguration {
    private String resource;
    private String host;
    private int port;
    private String secret;

    @Override
    public String getName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getSecret(JID address) {
        return secret;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
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
