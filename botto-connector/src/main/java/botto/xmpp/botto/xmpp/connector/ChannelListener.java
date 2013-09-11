package botto.xmpp.botto.xmpp.connector;

import org.xmpp.packet.Packet;

public interface ChannelListener {
    public void onIncomingPacket(Channel channel, Packet packet);
    public void onOutgoingPacket(Channel channel, Packet packet);
    public void channelEvent(ChannelEvent event);
}
