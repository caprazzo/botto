package botto.xmpp.botto.xmpp.connector;

import com.google.common.base.Preconditions;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Base class for implementations of low-level XMPP Connectors.
 */
// TODO: implement address and channel validation
public abstract class Connector<TConfig extends ConnectorConfiguration, TConnection extends BotConnection> {

    private final TConfig configuration;
    private final String name;
    private ConnectorPacketLstener listener;
    private ChannelListener channelListener;

    private final ConcurrentHashMap<Channel, TConnection> connections = new ConcurrentHashMap<Channel, TConnection>();

    public abstract void doOpenChannel(Channel channel) throws ConnectorException;
    public abstract void doCloseChannel(Channel channel) throws ConnectorException;
    public abstract void doStart() throws ConnectorException;
    public abstract void doStop() throws ConnectorException;
    public abstract void doSend(Channel channel, Packet packet) throws ConnectorException;

    public Connector(TConfig configuration) {
        // TODO: check configuration has a minimum validity
        this.configuration = configuration;
        this.name = configuration.getName();
    }

    public Channel openChannel(JID address) throws ConnectorException {
        Channel channel = Channel.from(address);
        doOpenChannel(channel);
        this.channelListener.onChannelOpen(channel);
        return channel;
    }

    public void closeChannel(Channel channel) throws ConnectorException {
        Preconditions.checkNotNull(channel);
        doCloseChannel(channel);
        this.channelListener.onChannelClose(channel);
    }

    public void addChannelListener(ChannelListener channelListener) {
        Preconditions.checkNotNull(channelListener);
        this.channelListener = channelListener;
    }

    public void send(Channel channel, Packet packet) throws ConnectorException {
        Preconditions.checkNotNull(channel, packet);
        channelListener.onOutgoingPacket(channel, packet);
        doSend(channel, packet);
    }

    public final String getName() {
        return name;
    }

    public final void start() throws ConnectorException {
        // TODO catch and log
        doStart();
    }

    public final void stop() throws ConnectorException {
        // TODO catch and log
        doStop();
    }

    // TODO: allow setting multiple listeners
    public final void setPacketListener(ConnectorPacketLstener listener) {
        Preconditions.checkNotNull(listener);
        this.listener = listener;
    }

    protected final void receive(BotConnection connection, Packet packet) throws ConnectorException {
        Preconditions.checkNotNull(connection);
        Preconditions.checkNotNull(packet);
        // TODO: catch and log
        listener.onIncoming(connection, packet);
    }

    protected final TConfig getConfiguration() {
        return configuration;
    }

    protected final void addConnection(Channel channel, TConnection connection) throws ConnectorException {
        Preconditions.checkNotNull(channel);
        Preconditions.checkNotNull(connection);
        TConnection existing = connections.putIfAbsent(channel, connection);
        if (connection == existing) {
            throw new ConnectorException("A connection for channel {} already exists: {}", channel, existing);
        }
    }

    protected final TConnection getConnection(Channel channel) {
        Preconditions.checkNotNull(channel);
        return connections.get(channel);
    }

    protected final TConnection removeConnection(Channel channel) throws ConnectorException {
        Preconditions.checkNotNull(channel);
        TConnection removed = connections.remove(channel);
        if (removed == null) {
            throw new ConnectorException("No connection found for channel {}", channel);
        }
        return removed;
    }

}
