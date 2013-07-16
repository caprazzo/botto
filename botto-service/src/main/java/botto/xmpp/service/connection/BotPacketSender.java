package botto.xmpp.service.connection;

import botto.xmpp.service.utils.QueueExecutor;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Packet;

/**
 * Async packet sender.
 * TODO: don't dequeue if the connection is not available
 */
public class BotPacketSender extends QueueExecutor<BotPacketSender.ConnectionPacket> {
    @Override
    public void doProcess(ConnectionPacket packet) {
        if (validateConnection(packet.getConnection())) {
            packet.getConnection().sendPacket(packet.getPacket());
        }
    }

    private boolean validateConnection(XMPPConnection connection) {
        return connection.isConnected() && connection.isAuthenticated();
    }

    public void send(XMPPConnection connection, Packet packet) {
        enqueue(new ConnectionPacket(connection, packet));
    }

    public final class ConnectionPacket {

        private final XMPPConnection connection;
        private final Packet packet;

        public ConnectionPacket(XMPPConnection connection, Packet packet) {
            this.connection = connection;
            this.packet = packet;
        }


        public XMPPConnection getConnection() {
            return connection;
        }

        public Packet getPacket() {
            return packet;
        }
    }
}
