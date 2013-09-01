package botto.xmpp.connectors.mock;

import botto.xmpp.botto.xmpp.connector.BotConnection;
import botto.xmpp.botto.xmpp.connector.Connector;
import botto.xmpp.botto.xmpp.connector.ConnectorException;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MockConnector extends Connector<MockConnectorConfiguration> {

    public MockConnector(MockConnectorConfiguration configuration) {
        super(configuration);
    }

    Map<String, MockBotConnection> bots = new ConcurrentHashMap<String, MockBotConnection>();

    @Override
    public BotConnection createConnection(JID address) throws ConnectorException {
        MockBotConnection connection = new MockBotConnection(this, address);
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
        // delay by 1s
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        // if found a connection for this destination, deliver directly to it
        // (a normal connector would send to a server instead)
        MockBotConnection connection = bots.get(packet.getTo().toBareJID());
        if (connection != null) {
            receive(connection, packet);
        }
    }
}
