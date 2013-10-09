package botto.xmpp;

import botto.xmpp.annotations.BotContext;
import botto.xmpp.annotations.PacketOutput;
import org.xmpp.packet.Packet;

class TestBot extends AbstractBot {

    private PacketOutput getOutput() {
        return output;
    }

    private PacketOutput output;

    @Override
    protected void doSetPacketOutput(PacketOutput output) {
        this.output = output;
    }

    @Override
    protected Packet doReceive(Packet packet) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void doSetcontext(BotContext botContext) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void send(Packet packet) {
        this.output.send(packet);
    }
}
