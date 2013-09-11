package botto.xmpp;

import botto.xmpp.annotations.BotContext;
import botto.xmpp.annotations.PacketOutput;
import org.xmpp.packet.Packet;

public abstract class AbstractBot implements Bot {

    public final void setPacketOutput(PacketOutput output) {
        doSetPacketOutput(output);
    }

    public final void setConnectionInfo(BotContext botContext) {
        doSetConnectionInfo(botContext);
    }

    @Override
    public final Packet receive(Packet packet) {
        return doReceive(packet);
    }

    protected abstract void doSetPacketOutput(PacketOutput output);

    protected abstract Packet doReceive(Packet packet);

    protected abstract void doSetConnectionInfo(BotContext botContext);
}
