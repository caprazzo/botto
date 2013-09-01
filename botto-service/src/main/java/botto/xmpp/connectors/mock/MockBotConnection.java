package botto.xmpp.connectors.mock;

import botto.xmpp.botto.xmpp.connector.BotConnection;
import botto.xmpp.botto.xmpp.connector.BotConnectionInfo;
import botto.xmpp.botto.xmpp.connector.ConnectionInfoListener;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

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
    public void send(Packet packet) {
        connector.send(packet);
    }

}
