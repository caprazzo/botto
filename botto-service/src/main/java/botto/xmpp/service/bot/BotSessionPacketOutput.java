package botto.xmpp.service.bot;

import botto.xmpp.annotations.PacketOutput;
import botto.xmpp.utils.PacketTypeConverter;
import com.google.common.base.Preconditions;
import org.jivesoftware.smack.XMPPConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.Packet;

/**
 * Implementation of packetOutput that uses a BotSessionPacketSender to send packets,
 * and converts outgoing packets from Smack model to Tinder Model.
 */
public class BotSessionPacketOutput implements PacketOutput {

    private final Logger Log = LoggerFactory.getLogger(BotSessionPacketOutput.class);

    private final BotSessionPacketSender sender;
    private final XMPPConnection connection;
    private final BotSession session;

    public BotSessionPacketOutput(BotSessionPacketSender sender, XMPPConnection connection, BotSession session) {
        this.sender = sender;
        this.connection = connection;
        this.session = session;
    }

    @Override
    public void send(Packet packet) {
        Preconditions.checkNotNull(packet, "Packet can't be null");
        try {
            sender.send(session, PacketTypeConverter.convertFromTinder(packet, connection));
        }
        catch (Exception ex) {
            Log.error("Error while sending packet {}: {}", packet, ex);
        }
    }
}
