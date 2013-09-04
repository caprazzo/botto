package botto.xmpp.botto.xmpp.connector;

import org.xmpp.packet.Packet;

public interface ConnectorPacketLstener {
    void onIncoming(BotConnection connection, Packet packet);
    void onOutgoing(BotConnection connection, Packet packet);
}
