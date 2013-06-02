package net.caprazzi.xmpp.bot.api;

import org.xmpp.packet.Packet;

public interface PacketOutput {
    void send(Packet packet);
}
