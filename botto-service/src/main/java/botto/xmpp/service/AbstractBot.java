package botto.xmpp.service;

import botto.xmpp.annotations.ConnectionInfo;
import botto.xmpp.annotations.PacketOutput;
import org.xmpp.packet.Packet;

public abstract class AbstractBot implements Bot {
    private PacketOutput output;

    public final void setPacketOutput(PacketOutput output) {
        this.output = output;
        doSetPacketOutput(output);
    }

    public final void setConnectionInfo(ConnectionInfo connectionInfo) {
        doSetConnectionInfo(connectionInfo);
    }

    @Override
    public final Packet receive(Packet packet) {
        return doReceive(packet);
    }

    protected abstract void doSetPacketOutput(PacketOutput output);

    protected abstract Packet doReceive(Packet packet);

    protected abstract void doSetConnectionInfo(ConnectionInfo connectionInfo);
}
