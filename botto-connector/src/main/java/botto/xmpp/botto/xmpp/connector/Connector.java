package botto.xmpp.botto.xmpp.connector;

import botto.xmpp.botto.xmpp.connector.channel.*;
import com.google.common.base.Preconditions;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Base class for implementations of low-level XMPP Connectors.
 *
 * A Connector handles xmpp connections
 * It allows to send/receive packets from/to an Address
 * A Channel is an immutable binding between an Address and a Connector
 * A ChannelContext is a mutable descriptor of a Channel
 */
// TODO: implement address and channel validation
public abstract class Connector<TConfig extends ConnectorConfiguration, TConnection extends ChannelConnection> {

    private final Logger Log = LoggerFactory.getLogger(this.getClass());

    private final ConnectorId connectorId;
    private final TConfig configuration;
    private final String name;
    private ChannelListener channelListener;

    private final ConcurrentHashMap<Channel, TConnection> connections = new ConcurrentHashMap<Channel, TConnection>();

    public abstract void doOpenChannel(Channel channel) throws ConnectorException;
    public abstract void doCloseChannel(Channel channel) throws ConnectorException;
    public abstract void doStart() throws ConnectorException;
    public abstract void doStop() throws ConnectorException;
    public abstract void doSend(Channel channel, Packet packet) throws ConnectorException;

    /**
     * @param connectorId
     * @param configuration
     */
    public Connector(ConnectorId connectorId, TConfig configuration) {
        this.connectorId = connectorId;
        // TODO: check configuration has a minimum validity
        this.configuration = configuration;
        this.name = configuration.getName();
    }

    /**
     * Open a channel for this address.
     * @param address the address
     * @return a ChannelContext for this address
     * @throws ConnectorException
     */
    public ChannelContext openChannel(JID address) throws ConnectorException {
        Channel channel = Channel.from(getConnectorId(), address);
        ChannelContext context = ChannelContext.of(channel);
        setChannelEvent(ChannelEvent.opening(channel));
        doOpenChannel(channel);
        return context;
    }

    public void closeChannel(Channel channel) throws ConnectorException {
        Preconditions.checkNotNull(channel);
        setChannelEvent(ChannelEvent.closing(channel));
        doCloseChannel(channel);
        setChannelEvent(ChannelEvent.closed(channel));
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

    protected final void receive(Channel channel, Packet packet) throws ConnectorException {
        Preconditions.checkNotNull(channel, "Channel can't be null");
        Preconditions.checkNotNull(packet, "Packet can't be null");
        try {
            channelListener.onIncomingPacket(channel, packet);
        }
        catch(Exception ex) {
            Log.error("Error while delivering incoming packet to listener {}: {}:" + ex, channel, packet.toXML());
        }
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

    protected void setChannelEvent(ChannelEvent event) {
        channelListener.onChannelEvent(event);
    }

    public ConnectorId getConnectorId() {
        return connectorId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("id", connectorId)
            .toString();
    }
}
