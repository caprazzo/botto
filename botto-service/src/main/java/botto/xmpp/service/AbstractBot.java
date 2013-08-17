package botto.xmpp.service;

import botto.xmpp.annotations.ConnectionStatus;
import botto.xmpp.annotations.PacketOutput;
import org.xmpp.packet.Packet;

public abstract class AbstractBot implements Bot {
    private PacketOutput output;
    private ConnectionStatus connectionStatus;

    public final void setPacketOutput(PacketOutput output) {
        this.output = output;
        doSetPacketOutput(output);
    }

    protected abstract void doSetPacketOutput(PacketOutput output);

    @Override
    public final Packet receive(Packet packet) {
        Packet response = doReceive(packet);
         if (response != null) {
            output.send(response);
        }
        return response;
    }

    protected abstract Packet doReceive(Packet packet);

    public void setConnectionStatus(ConnectionStatus connectionStatus) {
        this.connectionStatus = connectionStatus;
        doSetConnectionStatus(connectionStatus);
    }

    protected abstract void doSetConnectionStatus(ConnectionStatus connectionStatus);
}
