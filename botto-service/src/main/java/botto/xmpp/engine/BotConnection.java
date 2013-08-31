package botto.xmpp.engine;

import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;


/**
 * Represents the binding between a Bot and the underlying Connector
 */
public interface BotConnection  {



    public interface ConnectionPacketListener {
        public void onPacket(Packet packet);
    }

    public BotConnectionInfo getConnectionInfo();
    public void setConnectionInfoListener(ConnectionInfoListener infoListener);
    public void setConnectionPacketListener(ConnectionPacketListener packetListener);

    public JID getSendAddress();
    public void send(Packet packet);

    // TODO: send packets
    // TODO: receive packets
}
