package botto.xmpp.connectors.smack;

import botto.xmpp.engine.BotConnection;
import botto.xmpp.engine.Connector;
import botto.xmpp.engine.ConnectorConfiguration;
import botto.xmpp.engine.ConnectorException;
import botto.xmpp.service.AbstractBot;
import org.xmpp.packet.JID;

import java.util.Map;

/**
 * A connector that uses the Smack library for single-node bots.
 */
public class SmackConnector extends Connector<SmackConnectorconfiguration> {

    private final SmackConnectorconfiguration configuration;

    public SmackConnector(SmackConnectorconfiguration configuration) {
        this.configuration = configuration;
    }

    public BotConnection createConnection(JID address) {
        return new SmackBotConnection(address, this.configuration.getHost(), this.configuration.getPort(), this.configuration.getSecret(address), this.configuration.getResource() );
    }

    @Override
    public void removeConnection(BotConnection connection) throws ConnectorException {
        if (!(connection instanceof SmackBotConnection)) {
            throw new ConnectorException(new IllegalArgumentException("Can only remove connections of type WhackBotConection"));
        }
    }

    @Override
    public void start() {
        // TODO: all connections should only be started here
    }

    @Override
    public void stop() {
        // TODO: should stop all connections here
    }

}
