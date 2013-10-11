package botto.xmpp.connectors.smack;

import botto.xmpp.botto.xmpp.connector.*;

import botto.xmpp.botto.xmpp.connector.channel.Channel;
import botto.xmpp.botto.xmpp.connector.channel.ChannelEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xmpp.packet.Packet;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A connector that uses the Smack library for single-node bots.
 */
public class SmackConnector extends Connector<SmackConnectorConfiguration, SmackChannelConnection> {

    private final Logger Log = LoggerFactory.getLogger(SmackConnector.class);

    public SmackConnector(SmackConnectorConfiguration configuration) {
        super(configuration);
        checkNotNull(configuration, "Configuration must not be null");
    }

    @Override
    public void doOpenChannel(Channel channel) throws ConnectorException {
        SmackChannelConnection connection = new SmackChannelConnection(this, channel, getConfiguration().getHost(), getConfiguration().getPort(), getConfiguration().getSecret(channel.getAddress()), getConfiguration().getResource());
        addConnection(channel, connection);
        connection.start();
        setChannelEvent(ChannelEvent.opened(channel));
        return;
    }

    void channelEvent(ChannelEvent event) {
        setChannelEvent(event);
    }

    @Override
    public void doCloseChannel(Channel channel) throws ConnectorException {
        removeConnection(channel).stop();
    }

    @Override
    public void doStart() throws ConnectorException {
        // TODO: all connections should only be started here
    }

    @Override
    public void doStop()  throws ConnectorException {
        // TODO: should stop all connections here
    }

    @Override
    public void doSend(Channel channel, Packet packet) throws ConnectorException {
        getConnection(channel).send(packet);
    }

    public void receiveFromConnection(Channel channel, org.xmpp.packet.Packet packet) throws ConnectorException {
        receive(channel, packet);
    }
}
