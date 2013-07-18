package botto.xmpp.service.bot;

import org.jivesoftware.smack.packet.Packet;

public final class BotSessionPacket {

    private final BotSession session;
    private final Packet packet;

    public BotSessionPacket(BotSession session, Packet packet) {
        this.session = session;
        this.packet = packet;
    }


    public BotSession getSession() {
        return session;
    }

    public Packet getPacket() {
        return packet;
    }
}
