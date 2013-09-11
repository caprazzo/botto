package botto.xmpp.botto.xmpp.connector;

import botto.xmpp.botto.xmpp.connector.channel.Channel;

/**
 * Represents the binding between a Bot and the underlying Connector
 */
public interface BotConnection  {

    public BotConnectionInfo getConnectionInfo();
    public void setConnectionInfoListener(ConnectionInfoListener infoListener);

    public Channel getChannel();

    public Connector getConnector();

}
