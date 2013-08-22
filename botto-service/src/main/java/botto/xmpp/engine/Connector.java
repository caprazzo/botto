package botto.xmpp.engine;

import botto.xmpp.service.AbstractBot;
import net.caprazzi.reusables.common.Managed;
import org.xmpp.packet.JID;

/**
 * Interface for implementations of low-level XMPP Connectors.
 */
public abstract class Connector<TConfig extends ConnectorConfiguration> implements Managed {

    public abstract BotConnection createConnection(JID address) throws ConnectorException;
    public abstract void removeConnection(BotConnection connection) throws ConnectorException;

    public abstract void start();
    public abstract void stop();

}
