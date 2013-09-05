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
        //To change body of created methods use File | Settings | File Templates.
    }

    @Override
    public void removeConnection(BotConnection connection) throws ConnectorException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void doStart() throws ConnectorException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void doStop()  throws ConnectorException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void doSend(BotConnection connection, Packet packet) throws ConnectorException {
        send(packet);
    }

    public void send(Packet packet) throws ConnectorException {
        // delay by 1s
        // delay(1000);


        // if found a connection for this destination, deliver directly to it
        // (a normal connector would send to a server instead)
        MockBotConnection connection = bots.get(packet.getTo().toBareJID());
        if (connection != null) {
            receive(connection, packet);
        }
    }

    private void delay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}
