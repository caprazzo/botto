package botto.xmpp.connectors.mock;

import botto.xmpp.engine.BotConnection;
import botto.xmpp.engine.Connector;
import botto.xmpp.engine.ConnectorException;
import org.xmpp.packet.JID;

public class MockConnector extends Connector<MockConnectorConfiguration> {

    public MockConnector(MockConnectorConfiguration configuration) {
        //To change body of created methods use File | Settings | File Templates.
    }

    @Override
    public BotConnection createConnection(JID address) throws ConnectorException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
}
