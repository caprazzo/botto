package botto.xmpp.botto.xmpp.connector;

import net.caprazzi.reusables.common.Managed;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

/**
 * Interface for implementations of low-level XMPP Connectors.
 */
public abstract class Connector<TConfig extends ConnectorConfiguration> implements Managed {

    private final TConfig configuration;
    private final String name;
    private ConnectorPacketLstener listener;

    public Connector(TConfig configuration) {
        // TODO: check configuration has a minimum validity
        this.configuration = configuration;
        this.name = configuration.getName();
    }

    public abstract BotConnection createConnection(JID address) throws ConnectorException;
    public abstract void removeConnection(BotConnection connection) throws ConnectorException;

    public final void setPacketListener(ConnectorPacketLstener listener) {
        this.listener = listener;
    }

    public void setOutgoingPacketListener(ConnectorPacketLstener connectorPacketLstener) {
        //To change body of created methods use File | Settings | File Templates.
    }

    protected final void receive(BotConnection connection, Packet packet) {
        listener.onIncoming(connection, packet);
    }

    public final void send(BotConnection connection, Packet packet) {
        listener.onOutgoing(connection, packet);
        doSend(connection, packet);
    }

    public final String getName() {
        return name;
    }

    protected TConfig getConfiguration() {
        return configuration;
    }

    public abstract void start();
    public abstract void stop();
    public abstract void doSend(BotConnection connection, Packet packet);


}
