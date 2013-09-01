package botto.xmpp.connectors.mock;

import botto.xmpp.engine.BotConnection;
import botto.xmpp.engine.Connector;
import botto.xmpp.engine.ConnectorException;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MockConnector extends Connector<MockConnectorConfiguration> {

    private final MockConnectorConfiguration configuration;

    public MockConnector(MockConnectorConfiguration configuration) {
        this.configuration = configuration;
    }

    Map<String, MockBotConnection> bots = new ConcurrentHashMap<String, MockBotConnection>();

    @Override
    public BotConnection createConnection(JID address) throws ConnectorException {
        MockBotConnection connection = new MockBotConnection(address, this);
        bots.put(address.toBareJID(), connection);
        return connection;
    }

    @Override
    public void removeConnection(BotConnection connection) throws ConnectorException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void start() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void stop() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void send(Packet packet) {
        MockBotConnection connection = bots.get(packet.getTo().toBareJID());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        if (connection != null) {
            connection.receive(packet);
        }
    }
}
