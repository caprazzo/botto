package botto.xmpp.connectors.smack;

import botto.xmpp.botto.xmpp.connector.BotConnection;
import botto.xmpp.botto.xmpp.connector.Connector;
import botto.xmpp.botto.xmpp.connector.ConnectorException;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xmpp.packet.JID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A connector that uses the Smack library for single-node bots.
 */
public class SmackConnector extends Connector<SmackConnectorConfiguration> {

    private final Logger Log = LoggerFactory.getLogger(SmackConnector.class);

    public SmackConnector(SmackConnectorConfiguration configuration) {
        super(configuration);
        checkNotNull(configuration, "Configuration must not be null");

    }

    public BotConnection createConnection(JID address) {
        checkNotNull(address, "addresss must not be null");

        SmackBotConnection connection = new SmackBotConnection(this, address, getConfiguration().getHost(), getConfiguration().getPort(), getConfiguration().getSecret(address), getConfiguration().getResource());
        // TODO: connection should start only if connector.start() has been invoked
        connection.start();
        return connection;
    }

    @Override
    public void removeConnection(BotConnection connection) throws ConnectorException {
        checkNotNull(connection, "connection must not be null");
        if (!(connection instanceof SmackBotConnection)) {
            throw new ConnectorException(new IllegalArgumentException("Can only remove connections of type WhackBotConection"));
        }
        SmackBotConnection conn = (SmackBotConnection)connection;
        // TODO: connection should stop() only if it was started
        conn.stop();
    }

    @Override
    public void start() {
        // TODO: all connections should only be started here
    }

    @Override
    public void stop() {
        // TODO: should stop all connections here
    }

    public void receiveFromConnection(SmackBotConnection connection, org.xmpp.packet.Packet packet) {
        receive(connection, packet);
    }
}
