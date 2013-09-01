package botto.xmpp.engine;

import botto.xmpp.botto.xmpp.connector.*;
import botto.xmpp.service.AbstractBot;
import botto.xmpp.service.dispatcher.DispatcherService;
import com.google.common.base.Objects;
import net.caprazzi.reusables.common.Managed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionManager implements Managed {

    private final Logger Log = LoggerFactory.getLogger(ConnectionManager.class);

    private DispatcherService dispatcher = new DispatcherService();
    private Map<ConnectionKey, BotConnection> connections = new HashMap<ConnectionKey, BotConnection>();

    private Map<ConnectorId, Connector> connectors = new HashMap<ConnectorId, Connector>();

    private final AtomicInteger connectorCount = new AtomicInteger();

    // TODO: return connectorId
    public synchronized ConnectorId registerConnector(Connector connector) throws Exception {

        if(connectors.containsValue(connector)) {
            // TODO: throw. connectors can only be registered once
            Log.error("Could not register connector {} beacause it has already been registered");
            // TODO: use a project-specific exception
            throw new Exception("Could not register connector " + connector);
        }

        ConnectorId id = new ConnectorId(connectorCount.getAndIncrement(), connector.getClass(), connector.getName());
        connectors.put(id, connector);

        // TODO: prevent adding a connector twice
        // TODO: maybe should have Connector Registry?
        connector.setPacketListener(new ConnectorPacketLstener() {
            @Override
            public void onPacket(BotConnection connection, Packet packet) {
                dispatcher.receive(connection, packet);
            }
        });

        Log.info("Registered connector {} with id {}", connector, id);

        return id;
    }

    public synchronized void removeConnector(ConnectorId connectorId) {
        // TODO: implement removal code
    }

    public synchronized void addBot(AbstractBot bot, JID address, ConnectorId connectorId) throws Exception {
        Connector connector = connectors.get(connectorId);
        if (connector == null) {
            throw new Exception("Connector not found");
        }
        BotConnection connection = connector.createConnection(address);
        connections.put(new ConnectionKey(bot, address, connector), connection);
        bot.setConnectionInfo(connection.getConnectionInfo());
        dispatcher.addConnection(bot, connection);
    }

    public synchronized void removeBot(AbstractBot bot, JID address, ConnectorId connectorId) throws Exception {
        Connector connector = connectors.get(connectorId);
        if (connector == null) {
            throw new Exception("Connector not found");
        }
        BotConnection connection = connections.get(new ConnectionKey(bot, address, connector));
        if (connection != null) {
            dispatcher.removeConnection(bot, connection);
            connector.removeConnection(connection);
        }
    }

    @Override
    public void start() {
        dispatcher.start();
    }

    @Override
    public void stop() {
        dispatcher.stop();
    }

    private static class ConnectionKey {
        private final AbstractBot bot;
        private final JID address;
        private final Connector connector;

        public ConnectionKey(AbstractBot bot, JID address, Connector connector) {
            this.bot = bot;
            this.address = address;
            this.connector = connector;
        }

        @Override
        public int hashCode() {
            return com.google.common.base.Objects.hashCode(bot, address, connector);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;

            if (getClass() != obj.getClass())
                return false;

            final ConnectionKey other = (ConnectionKey) obj;
            return Objects.equal(bot, other.bot)
                    && Objects.equal(address, other.address)
                    && Objects.equal(connector, other.connector);
        }
    }

}
