package botto.xmpp.botto.xmpp.connector.channel;

import org.xmpp.packet.Packet;

public interface ChannelListener {
    public void onIncomingPacket(Channel channel, Packet packet);
    public void onOutgoingPacket(Channel channel, Packet packet);
    public void onChannelEvent(ChannelEvent event);
}
