package botto.xmpp.connectors.smack;

import botto.xmpp.engine.BotConnection;
import botto.xmpp.engine.Connector;
import botto.xmpp.engine.ConnectorException;
import org.xmpp.packet.JID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A connector that uses the Smack library for single-node bots.
 */
public class SmackConnector extends Connector<SmackConnectorConfiguration> {

    private final SmackConnectorConfiguration configuration;

    public SmackConnector(SmackConnectorConfiguration configuration) {
        checkNotNull(configuration, "Configuration must not be null");
        this.configuration = configuration;
    }

    public BotConnection createConnection(JID address) {
        checkNotNull(address, "addresss must not be null");
        return new SmackBotConnection(address, this.configuration.getHost(), this.configuration.getPort(), this.configuration.getSecret(address), this.configuration.getResource() );
    }

    @Override
    public void removeConnection(BotConnection connection) throws ConnectorException {
        checkNotNull(connection, "connection must not be null");
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
