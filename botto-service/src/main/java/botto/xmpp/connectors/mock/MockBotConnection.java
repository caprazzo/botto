package botto.xmpp.connectors.mock;

import botto.xmpp.engine.BotConnection;
import botto.xmpp.engine.BotConnectionInfo;
import botto.xmpp.engine.ConnectionInfoListener;
import botto.xmpp.service.Bot;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

public class MockBotConnection implements BotConnection {

    private final JID address;
    private final MockConnector connector;
    private ConnectionPacketListener packetListener;

    private final BotConnectionInfo connectionInfo = new BotConnectionInfo();

    public MockBotConnection(JID address, MockConnector connector) {
        this.address = address;
        this.connector = connector;
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
    public void setConnectionPacketListener(ConnectionPacketListener packetListener) {
        this.packetListener = packetListener;
    }

    @Override
    public JID getSendAddress() {
        return address;
    }

    @Override
    public void send(Packet packet) {
        connector.send(packet);
    }

    public void receive(Packet packet) {
        packetListener.onPacket(packet);
    }
}
