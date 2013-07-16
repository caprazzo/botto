package botto.xmpp.service.connection;

import botto.xmpp.annotations.PacketOutput;
import botto.xmpp.service.AbstractBot;
import botto.xmpp.service.reflection.AnnotatedBotObject;
import botto.xmpp.utils.PacketTypeConverter;

import com.google.common.base.Preconditions;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;

import org.xmpp.packet.Packet;

import java.util.HashMap;

public class BotSessionManager {

    private final String host;
    private final int port;

    private final HashMap<String, XMPPConnection> connections = new HashMap<String, XMPPConnection>();

    private final BotPacketSender sender = new BotPacketSender();

    public BotSessionManager(String host, int port) {
        this.host = host;
        this.port = port;
    }

    // TODO: each connection should be enclosed in its own thread
    // TODO: incoming packets should all go to the same queue
    // TODO: outgoing packets should be put in a queue

    public void createSession(final AbstractBot bot, final String node, String secret, String resource) {
        ConnectionConfiguration configuration = new ConnectionConfiguration(host, port);
        configuration.setReconnectionAllowed(true);
        configuration.setSendPresence(true);
        configuration.setCompressionEnabled(true);

        final XMPPConnection connection = new XMPPConnection(configuration);
        connections.put(node, connection);

        connection.addPacketListener(new PacketListener() {
            @Override
            public void processPacket(org.jivesoftware.smack.packet.Packet packet) {
                Packet response = bot.receive(PacketTypeConverter.converttoTinder(packet));
                if (response != null) {
                    sender.send(connection, packet);
                }
            }
        }, null);

        bot.setPacketOutput(new PacketOutput() {
            @Override
            public void send(Packet packet) {
                Preconditions.checkNotNull(packet, "Packet can't be null");
                sender.send(connection, PacketTypeConverter.convertFromTinder(packet, connection));
            }
        });
    }

    public void start() {
        // connect all

        for(XMPPConnection connection : connections.values()) {
            connection.connect();

            connection.login();

        }

        // start sender
    }

    public void shutdown() {

    }

}
