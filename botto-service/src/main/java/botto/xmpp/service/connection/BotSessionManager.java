package botto.xmpp.service.connection;

import botto.xmpp.service.reflection.AnnotatedBotObject;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;

import org.xmpp.packet.Packet;

import java.util.HashMap;

public class BotSessionManager {

    private final String host;
    private final int port;

    private final HashMap<String, XMPPConnection> connections = new HashMap<String, XMPPConnection>();

    public BotSessionManager(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void createSession(final AnnotatedBotObject bot, String node, String secret, String resource) {
        ConnectionConfiguration configuration = new ConnectionConfiguration(host, port);
        configuration.setReconnectionAllowed(true);
        configuration.setSendPresence(true);
        configuration.setCompressionEnabled(true);
        XMPPConnection connection = new XMPPConnection(configuration);
        connections.put(node, connection);

        connection.addPacketListener(new PacketListener() {
            @Override
            public void processPacket(org.jivesoftware.smack.packet.Packet packet) {
                bot.doReceive(packet);
            }
        }, null);
    }

    public void send(String node, Packet packet) {

    }

}
