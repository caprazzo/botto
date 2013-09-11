package botto.xmpp.connectors.mock;

import botto.xmpp.botto.xmpp.connector.channel.Channel;
import botto.xmpp.botto.xmpp.connector.Connector;
import botto.xmpp.botto.xmpp.connector.ConnectorException;
import org.xmpp.packet.Packet;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MockConnector extends Connector<MockConnectorConfiguration, MockBotConnection> {

    ExecutorService executor = Executors.newCachedThreadPool();

    public MockConnector(MockConnectorConfiguration configuration) {
        super(configuration);
    }

    @Override
    public void doOpenChannel(Channel channel) throws ConnectorException {
        MockBotConnection connection = new MockBotConnection(this, channel);
        addConnection(channel, connection);
    }

    @Override
    public void doCloseChannel(Channel channel) throws ConnectorException {
        removeConnection(channel);
    }

    @Override
    public void doStart() throws ConnectorException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void doStop() throws ConnectorException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void doSend(final Channel channel, final Packet packet) throws ConnectorException {
        final MockBotConnection connection = getConnection(channel);
        executor.submit(new Runnable() {
            @Override
            public void run() {
                ;
                try {
                    receive(channel, packet);
                } catch (ConnectorException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        });
    }

    private void delay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}
