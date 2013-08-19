package botto.xmpp.connectors;

import botto.xmpp.engine.BotConnection;
import botto.xmpp.engine.Connector;
import botto.xmpp.engine.ConnectorException;
import botto.xmpp.service.AbstractBot;
import org.xmpp.packet.JID;

/**
 * A connector that uses the Smack library for single-node bots.
 */
public class SmackConnector implements Connector {

    // TODO: connector should be configured with host, port, domain, a map node/secret/resource

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

}
