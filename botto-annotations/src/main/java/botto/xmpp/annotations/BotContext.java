package botto.xmpp.annotations;

import org.xmpp.packet.Packet;

public interface BotContext {
    public boolean isConnected();
    public void send(Packet packet);
}
