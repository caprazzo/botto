package botto.xmpp.connectors.mock;

import botto.xmpp.botto.xmpp.connector.*;
import botto.xmpp.botto.xmpp.connector.channel.Channel;
import botto.xmpp.botto.xmpp.connector.channel.ChannelConnection;

public class MockBotConnection implements ChannelConnection {

    private final MockConnector connector;
    private final Channel channel;

    public MockBotConnection(MockConnector connector, Channel channel) {
        this.connector = connector;
        this.channel = channel;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public Connector getConnector() {
        return connector;
    }
}
