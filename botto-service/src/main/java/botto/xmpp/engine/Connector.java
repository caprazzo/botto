package botto.xmpp.engine;

import botto.xmpp.service.AbstractBot;
import net.caprazzi.reusables.common.Managed;
import org.xmpp.packet.JID;

/**
 * Interface for implementations of low-level XMPP Connectors
 */
public abstract class Connector<TConfig extends ConnectorConfiguration> implements Managed {
    public abstract void configure(TConfig configuration);
    public abstract BotConnection createConnection(AbstractBot bot, JID address) throws ConnectorException;
    public abstract void removeConnection(BotConnection connection) throws ConnectorException;

    public abstract void doStart();
    public abstract void doStop();

    public void start() {

    }

    public void stop()  {

    }
}
