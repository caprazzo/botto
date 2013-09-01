package botto.xmpp.connectors.whack;

import botto.xmpp.botto.xmpp.connector.BotConnection;
import botto.xmpp.botto.xmpp.connector.ConnectionInfoListener;
import botto.xmpp.botto.xmpp.connector.BotConnectionInfo;
import botto.xmpp.botto.xmpp.connector.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.component.ComponentException;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

class WhackBotConnection implements BotConnection {

    private static final Logger Log = LoggerFactory.getLogger(WhackBotConnection.class);

    private final WhackConnector connector;
    private final WhackBotComponent component;
    private final JID address;
    private BotConnectionInfo connectionInfo;

    public WhackBotConnection(WhackConnector connector, WhackBotComponent component, JID address) {
        this.connector = connector;
        this.component = component;
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

    public JID getSendAddress() {
        return address;
    }

    //public void receive(Packet packet) {
    //    packetListener.onPacket(packet);
    //}

    public void setConnectionInfo(BotConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }
}
