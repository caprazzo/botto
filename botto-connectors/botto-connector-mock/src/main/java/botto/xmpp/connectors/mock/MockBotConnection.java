package botto.xmpp.connectors.mock;

import botto.xmpp.botto.xmpp.connector.*;
import org.xmpp.packet.JID;

public class MockBotConnection implements BotConnection {

    private final MockConnector connector;
    private final Channel channel;

    private final BotConnectionInfo connectionInfo = new BotConnectionInfo();

    public MockBotConnection(MockConnector connector, Channel channel) {
        this.connector = connector;
        this.channel = channel;
    }

    @Override
    public BotConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    @Override
    public void setConnectionInfoListener(ConnectionInfoListener infoListener) {
        //To change body of implemented methods use File | Settings | File Templates.
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
