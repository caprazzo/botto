package botto.xmpp.service.dispatcher;

import botto.xmpp.utils.Helpers;
import com.google.common.base.Objects;
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

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("label", label)
            .add("packet", Helpers.toString(packet))
            .toString();
    }
}
