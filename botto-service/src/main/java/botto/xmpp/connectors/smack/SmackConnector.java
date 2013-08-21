package botto.xmpp.connectors.smack;

import botto.xmpp.engine.BotConnection;
import botto.xmpp.engine.Connector;
import botto.xmpp.engine.ConnectorConfiguration;
import botto.xmpp.engine.ConnectorException;
import botto.xmpp.service.AbstractBot;
import org.xmpp.packet.JID;

/**
 * A connector that uses the Smack library for single-node bots.
 */
public class SmackConnector extends Connector<SmackConnectorconfiguration> {

    // TODO: connector should be configured with host, port, domain, a map node/secret/resource
    @Override
    public void configure(SmackConnectorconfiguration configuration) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public BotConnection createConnection(AbstractBot bot, JID address) {
        //return new SmackBotConnection();
        return null;
    }

    @Override
    public void removeConnection(BotConnection connection) throws ConnectorException {
        if (!(connection instanceof SmackBotConnection)) {
            throw new ConnectorException(new IllegalArgumentException("Can only remove connections of type WhackBotConection"));
        }
    }

    @Override
    public void doStart() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void doStop() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

}
