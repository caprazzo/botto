package botto.xmpp;

import botto.xmpp.botto.xmpp.connector.*;
import botto.xmpp.botto.xmpp.connector.channel.Channel;
import botto.xmpp.botto.xmpp.connector.channel.ChannelEvent;
import botto.xmpp.botto.xmpp.connector.channel.ChannelListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.Packet;

class ConnectorChannelListener implements ChannelListener {

    private static final Logger Log = LoggerFactory.getLogger(ConnectorChannelListener.class);

    private final Meters.ConnectorMetrics meter;
    private final BotManager manager;
    private final Connector connector;

    public ConnectorChannelListener(BotManager manager, Connector connector, ConnectorId connectorId) {
        this.manager = manager;
        this.connector = connector;
        this.meter = Meters.connectors.forConnector(connectorId);
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
}
