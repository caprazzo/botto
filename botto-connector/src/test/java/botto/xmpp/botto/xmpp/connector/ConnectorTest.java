package botto.xmpp.botto.xmpp.connector;

import botto.xmpp.botto.xmpp.connector.channel.Channel;
import botto.xmpp.botto.xmpp.connector.channel.ChannelConnection;
import botto.xmpp.botto.xmpp.connector.channel.ChannelEvent;
import botto.xmpp.botto.xmpp.connector.channel.ChannelListener;
import org.junit.Test;
import org.mockito.Mockito;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ConnectorTest {

    @Test
    public void should_invoke_doOpenChannel() throws ConnectorException {
        JID address = mock(JID.class);

        TestConnectorConfig config = mock(TestConnectorConfig.class);
        ChannelListener listener = mock(ChannelListener.class);
        ConnectorId connectorId = mock(ConnectorId.class);

        TestConnector connector = new TestConnector(connectorId, config);
        connector.addChannelListener(listener);

        TestConnector spy = Mockito.spy(connector);

        spy.openChannel(address);

        ChannelEvent expected = ChannelEvent.opening(Channel.from(address));

        verify(listener).onChannelEvent(eq(expected));
        verify(spy).doOpenChannel(eq(Channel.from(address)));
    }

    public static class TestConnector extends Connector<TestConnectorConfig, TestConnectorConnection> {

        public TestConnector(ConnectorId connectorId, TestConnectorConfig configuration) {
            super(connectorId, configuration);
        }

        @Override
        public void doOpenChannel(Channel channel) throws ConnectorException {
            // TODO: missing method implementation
        }

        @Override
        public void doCloseChannel(Channel channel) throws ConnectorException {
            // TODO: missing method implementation
        }

        @Override
        public void doStart() throws ConnectorException {
            // TODO: missing method implementation
        }

        @Override
        public void doStop() throws ConnectorException {
            // TODO: missing method implementation
        }

        @Override
        public void doSend(Channel channel, Packet packet) throws ConnectorException {
            // TODO: missing method implementation
        }
    }

    public static class TestConnectorConfig implements ConnectorConfiguration {

        @Override
        public String getName() {
            return null;  // TODO: missing method implementation
        }
    }

    public static class TestConnectorConnection implements ChannelConnection {

        @Override
        public Channel getChannel() {
            return null;  // TODO: missing method implementation
        }

        @Override
        public Connector getConnector() {
            return null;  // TODO: missing method implementation
        }
    }
}
