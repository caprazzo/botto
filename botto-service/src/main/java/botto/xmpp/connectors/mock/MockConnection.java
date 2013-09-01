package botto.xmpp.connectors.mock;

import botto.xmpp.botto.xmpp.connector.BotConnection;
import botto.xmpp.botto.xmpp.connector.BotConnectionInfo;
import botto.xmpp.botto.xmpp.connector.ConnectionInfoListener;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

public class MockConnection implements BotConnection {

    @Override
    public BotConnectionInfo getConnectionInfo() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setConnectionInfoListener(ConnectionInfoListener infoListener) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public JID getSendAddress() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void send(Packet packet) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

}
