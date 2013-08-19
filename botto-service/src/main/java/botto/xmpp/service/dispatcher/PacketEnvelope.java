package botto.xmpp.service.dispatcher;

import org.xmpp.packet.Packet;

/**
 * A PacketEnvelope is a packet with an extra generic label.
 * This is used during routing of packets.
 * @param <TLabel>
 */
class PacketEnvelope<TLabel> {

    private final TLabel label;
    private final Packet packet;

    public PacketEnvelope(TLabel label, Packet packet) {
        this.label = label;
        this.packet = packet;
    }

    // TODO: 'source' is not really a great name here, as it is also used for delivering To
    public TLabel getLabel() {
        return label;
    }

    public Packet getPacket() {
        return packet;
    }
}
