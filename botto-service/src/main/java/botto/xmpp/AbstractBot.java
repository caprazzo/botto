package botto.xmpp;

import botto.xmpp.annotations.BotContext;
import org.xmpp.packet.Packet;

public abstract class AbstractBot implements Bot {

    public final void setContext(BotContext botContext) {
        doSetcontext(botContext);
    }

    @Override
    public final Packet receive(Packet packet) {
        return doReceive(packet);
    }

    protected abstract Packet doReceive(Packet packet);

    protected abstract void doSetcontext(BotContext botContext);
}
