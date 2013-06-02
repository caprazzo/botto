package net.caprazzi.xmpp.bot.service;

import org.xmpp.packet.Packet;

public interface Bot {
    public Packet receive(Packet packet);
}

