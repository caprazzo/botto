package botto.xmpp.botto.xmpp.connector.channel;

import botto.xmpp.botto.xmpp.connector.Connector;
import botto.xmpp.botto.xmpp.connector.channel.Channel;

/**
 * Represents the binding between a Channel and its underlying Connector
 */
public interface ChannelConnection {
    public Channel getChannel();
    public Connector getConnector();
}
