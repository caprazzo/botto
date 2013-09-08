package botto.xmpp.botto.xmpp.connector;

import org.xmpp.packet.Packet;

public interface ChannelListener {
    public void onChannelOpen(Channel channel);
    public void onChannelClose(Channel channel);
    public void onIncomingPacket(Channel channel, Packet packet);
    public void onOutgoingPacket(Channel channel, Packet packet);
}
