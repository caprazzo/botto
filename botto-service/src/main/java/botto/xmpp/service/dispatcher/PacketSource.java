package botto.xmpp.service.dispatcher;

import org.xmpp.packet.Packet;

public interface PacketSource {

    public interface PacketSourceListener {
        public void receive(Packet packet);
    }

    public void setPacketSourceListener(PacketSourceListener listener);
}
