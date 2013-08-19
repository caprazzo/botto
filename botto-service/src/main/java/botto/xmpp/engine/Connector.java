package botto.xmpp.engine;

import botto.xmpp.service.AbstractBot;
import org.xmpp.packet.JID;

/**
 * Interface for implementations of low-level XMPP Connectors
 */
public interface Connector {
    public BotConnection createConnection(AbstractBot bot, JID address) throws ConnectorException;
    public void removeConnection(BotConnection connection) throws ConnectorException;
}
