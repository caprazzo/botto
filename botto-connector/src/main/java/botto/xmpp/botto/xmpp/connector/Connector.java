package botto.xmpp.botto.xmpp.connector;

import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

/**
 * Base class for implementations of low-level XMPP Connectors.
 */
public abstract class Connector<TConfig extends ConnectorConfiguration> {

    private final TConfig configuration;
    private final String name;
    private ConnectorPacketLstener listener;

    public Connector(TConfig configuration) {
        // TODO: check configuration has a minimum validity
        this.configuration = configuration;
        this.name = configuration.getName();
    }

    public final void setPacketListener(ConnectorPacketLstener listener) {
        this.listener = listener;
    }

    protected final void receive(BotConnection connection, Packet packet) throws ConnectorException {
        // TODO: catch and log
        listener.onIncoming(connection, packet);
    }

    public final void send(BotConnection connection, Packet packet) throws ConnectorException {
        // TODO: catch and log
        listener.onOutgoing(connection, packet);
        doSend(connection, packet);
    }

    public final String getName() {
        return name;
    }

    protected TConfig getConfiguration() {
        return configuration;
    }


    public final void start() throws ConnectorException {
        // TODO catch and log
        doStart();
    }

    public final void stop() throws ConnectorException {
        // TODO catch and log
        doStop();
    }

    public abstract BotConnection createConnection(JID address) throws ConnectorException;
    public abstract void removeConnection(BotConnection connection) throws ConnectorException;
    public abstract void doStart() throws ConnectorException;
    public abstract void doStop() throws ConnectorException;
    public abstract void doSend(BotConnection connection, Packet packet) throws ConnectorException;
}
