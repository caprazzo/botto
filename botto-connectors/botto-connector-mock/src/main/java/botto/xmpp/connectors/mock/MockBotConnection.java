package botto.xmpp.connectors.mock;

import botto.xmpp.botto.xmpp.connector.BotConnection;
import botto.xmpp.botto.xmpp.connector.BotConnectionInfo;
import botto.xmpp.botto.xmpp.connector.ConnectionInfoListener;
import botto.xmpp.botto.xmpp.connector.Connector;
import org.xmpp.packet.JID;

public class MockBotConnection implements BotConnection {

    private final JID address;
    private final MockConnector connector;

    private final BotConnectionInfo connectionInfo = new BotConnectionInfo();

    public MockBotConnection(MockConnector connector, JID address) {
        this.connector = connector;
        this.address = address;
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
    public JID getSendAddress() {
        return address;
    }

    @Override
    public Connector getConnector() {
        return connector;
    }

}
