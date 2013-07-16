package botto.xmpp.service.connection;

import botto.xmpp.service.reflection.AnnotatedBotObject;
import botto.xmpp.utils.PacketTypeConverter;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;

import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

import javax.xml.parsers.SAXParser;
import java.util.HashMap;

public class BotSessionManager {

    private final String host;
    private final int port;

    private final HashMap<String, XMPPConnection> connections = new HashMap<String, XMPPConnection>();

    public BotSessionManager(String host, int port) {
        this.host = host;
        this.port = port;
    }

    // TODO: each connection should be enclosed in its own thread
    // TODO: incoming packets should all go to the same queue
    // TODO: outgoing packets should be put in a queue

    public void createSession(final AnnotatedBotObject bot, final String node, String secret, String resource) {
        ConnectionConfiguration configuration = new ConnectionConfiguration(host, port);
        configuration.setReconnectionAllowed(true);
        configuration.setSendPresence(true);
        configuration.setCompressionEnabled(true);
        XMPPConnection connection = new XMPPConnection(configuration);
        connections.put(node, connection);

        connection.addPacketListener(new PacketListener() {
            @Override
            public void processPacket(org.jivesoftware.smack.packet.Packet packet) {
                Packet response = bot.doReceive(PacketTypeConverter.converttoTinder(packet));
                if (response != null) {
                    send(node, response);
                }
            }
        }, null);
    }

    public void send(String node, Packet packet) {
        XMPPConnection connection = connections.get(node);
        if (connection != null && connection.isConnected() && connection.isAuthenticated()) {
            connection.sendPacket(PacketTypeConverter.convertFromTinder(packet, connection));
        }
    }

}
