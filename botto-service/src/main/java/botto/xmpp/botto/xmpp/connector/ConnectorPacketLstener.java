package botto.xmpp.botto.xmpp.connector;

import org.xmpp.packet.Packet;

public interface ConnectorPacketLstener {
    void onPacket(BotConnection connection, Packet packet);
}
