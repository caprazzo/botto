package botto.xmpp;

import botto.xmpp.annotations.BotContext;
import org.xmpp.packet.Packet;

class TestBot extends AbstractBot {

    private BotContext botContext;

    @Override
    protected Packet doReceive(Packet packet) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void doSetcontext(BotContext botContext) {
        this.botContext = botContext;
    }

    public void send(Packet packet) {
        this.botContext.send(packet);
    }
}
