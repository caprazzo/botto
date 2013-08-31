package botto.xmpp.connectors.mock;

import botto.xmpp.engine.BotConnection;
import botto.xmpp.engine.BotConnectionInfo;
import botto.xmpp.engine.ConnectionInfoListener;
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
    public void setConnectionPacketListener(ConnectionPacketListener packetListener) {
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
