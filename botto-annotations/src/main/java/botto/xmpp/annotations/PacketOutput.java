package botto.xmpp.annotations;

import org.xmpp.packet.Packet;

public interface PacketOutput {
    void send(Packet packet);
}
