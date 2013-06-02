package net.caprazzi.xmpp.bot.service;

import net.caprazzi.xmpp.bot.api.PacketOutput;
import org.xmpp.packet.Packet;

public abstract class AbstractBot implements Bot {
    private PacketOutput output;

    public final void setPacketOutput(PacketOutput output) {
        this.output = output;
        doSetPacketOutput(output);
    }

    protected abstract void doSetPacketOutput(PacketOutput output);

    @Override
    public final Packet receive(Packet packet) {
        output.send(doReceive(packet));
        return packet;
    }

    protected abstract Packet doReceive(Packet packet);
}
