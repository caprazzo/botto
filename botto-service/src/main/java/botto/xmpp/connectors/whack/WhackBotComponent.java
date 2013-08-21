package botto.xmpp.connectors.whack;

import botto.xmpp.engine.BotConnectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.component.Component;
import org.xmpp.component.ComponentException;
import org.xmpp.component.ComponentManager;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WhackBotComponent implements Component {

    private static final Logger Log = LoggerFactory.getLogger(WhackBotComponent.class);

    private final String subdomain;
    private JID jid;
    private ComponentManager componentManager;
    private Map<String, WhackBotConnection> connections = new ConcurrentHashMap<String, WhackBotConnection>();
    private final BotConnectionInfo connectionInfo = new BotConnectionInfo();

    public WhackBotComponent(String subdomain) {
        this.subdomain = subdomain;
    }

    public void addConnection(WhackBotConnection connection) {
        connection.setConnectionInfo(connectionInfo);
        connections.put(connection.getAddress().toBareJID(), connection);
    }

    public void removeConnection(WhackBotConnection connection) {
        connections.remove(connection.getAddress().toBareJID());
    }

    @Override
    public String getName() {
        return "Bot Component Manager for subdomain " + subdomain;
    }

    @Override
    public String getDescription() {
        return getName();
    }

    @Override
    public void processPacket(Packet packet) {
        WhackBotConnection connection = connections.get(packet.getFrom().toBareJID());
        if (connection == null) {
            // TODO: log
        }
        connection.receive(packet);
    }

    @Override
    public void initialize(JID jid, ComponentManager componentManager) throws ComponentException {
        this.jid = jid;
        this.componentManager = componentManager;
    }

    @Override
    public void start() {
        // TODO: does this mean 'connected'?
        Log.info("Start");
    }

    @Override
    public void shutdown() {
        Log.info("Shutdown");
    }

    public synchronized void send(Packet packet) throws ComponentException {
        componentManager.sendPacket(this, packet);
    }

    public boolean isEmpty() {
        return connections.isEmpty();
    }

    public String getSubdomain() {
        return subdomain;
    }

    public void setConnected(boolean connected) {
        connectionInfo.setConnectionStatus(connected);
    }
}
