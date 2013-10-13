package botto.xmpp;

import botto.xmpp.botto.xmpp.connector.*;
import botto.xmpp.botto.xmpp.connector.channel.Channel;
import botto.xmpp.botto.xmpp.connector.channel.ChannelEvent;
import botto.xmpp.botto.xmpp.connector.channel.ChannelListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.Packet;

import java.util.Objects;

/**
 * This ChannelListener routes Connector Channel Events to Bot Manager.
 * It also maintains some per-connector stats using Meters.
 * TODO: metering logic should be in a decorator
 */
class ConnectorChannelListener implements ChannelListener {

    private static final Logger Log = LoggerFactory.getLogger(ConnectorChannelListener.class);

    private final Meters.ConnectorMetrics meter;
    private final BotManager manager;
    private final Connector connector;

    public ConnectorChannelListener(BotManager manager, Connector connector) {
        this.manager = manager;
        this.connector = connector;
        this.meter = Meters.connectors.forConnector(connector.getConnectorId());
    }

    @Override
    public void onIncomingPacket(Channel channel, Packet packet) {
        meter.countIncoming(packet);
        manager.receive(connector, channel, packet, this.meter);
    }

    @Override
    public void onOutgoingPacket(Channel channel, Packet packet) {
        meter.countOutgoing(packet);
    }

    @Override
    public void onChannelEvent(ChannelEvent event) {
        Log.debug("{}", event);
        manager.setChannelEvent(event);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ConnectorChannelListener))
            return false;

        ConnectorChannelListener other = (ConnectorChannelListener) obj;
        return Objects.equals(manager, other.manager)
            && Objects.equals(connector, other.connector);
    }
}
