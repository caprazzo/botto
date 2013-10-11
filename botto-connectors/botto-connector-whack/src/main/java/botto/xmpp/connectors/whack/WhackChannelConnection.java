package botto.xmpp.connectors.whack;

import botto.xmpp.botto.xmpp.connector.*;
import botto.xmpp.botto.xmpp.connector.channel.Channel;
import botto.xmpp.botto.xmpp.connector.channel.ChannelConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.component.ComponentException;
import org.xmpp.packet.Packet;

class WhackChannelConnection implements ChannelConnection {

    private static final Logger Log = LoggerFactory.getLogger(WhackChannelConnection.class);

    private final WhackConnector connector;
    private final WhackBotComponent component;
    private final Channel channel;

    public WhackChannelConnection(WhackConnector connector, WhackBotComponent component, Channel channel) {
        this.connector = connector;
        this.component = component;
        this.channel = channel;
    }

    void send(Packet packet) {
        try {
            component.send(packet);
        } catch (ComponentException e) {
            Log.error("Error while sending packet {} to component {}", packet, component);
            throw new RuntimeException("Exception while sending packet " + packet, e);
        }
    }

    @Override
    public Connector getConnector() {
        return connector;
    }

    public Channel getChannel() {
        return channel;
    }
}
