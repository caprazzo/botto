package botto.xmpp.connectors.whack;

import botto.xmpp.botto.xmpp.connector.Channel;
import botto.xmpp.botto.xmpp.connector.Connector;
import botto.xmpp.botto.xmpp.connector.BotConnection;
import botto.xmpp.botto.xmpp.connector.ConnectorException;
import org.jivesoftware.whack.ExternalComponentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.component.ComponentException;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkNotNull;

public class WhackConnector extends Connector<WhackConnectorConfiguration, WhackBotConnection> {

    private static final Logger Log = LoggerFactory.getLogger(WhackConnector.class);

    private final ExternalComponentManager manager;

    private final Map<String, WhackBotComponent> components = new ConcurrentHashMap<String, WhackBotComponent>();

    public WhackConnector(WhackConnectorConfiguration configuration) {
        super(configuration);
        checkNotNull(configuration);

        // TODO the connector should be configured with host, port, domain, secret and a map subdomain -> password
        manager = new ExternalComponentManager(configuration.getHost(), configuration.getPort());
    }

    @Override
    public void doOpenChannel(Channel channel) throws ConnectorException {
        Log.debug("Creating Whack connection for {}", channel);
        verifyAddress(channel.getAddress());

        // get subdomain of bot
        String subdomain = getSubdomain(channel.getAddress());
        WhackBotComponent component = components.get(subdomain);
        if (component == null) {
            try {
                component = createComponent(subdomain, getConfiguration().getSecret(subdomain));
            } catch (ComponentException e) {
                throw new ConnectorException("Error while creating component for subdomain " + subdomain, e);
            }

        }

        WhackBotConnection connection = new WhackBotConnection(this, component, channel);
        component.addConnection(connection);
        addConnection(channel, connection);
    }

    @Override
    public void doCloseChannel(Channel channel) throws ConnectorException {
        WhackBotConnection connection = removeConnection(channel);
        for(WhackBotComponent component : components.values()) {
            component.removeConnection(connection);
            if (component.isEmpty()) {
                removeComponent(component);
            }
            return;
        }
    }

    private WhackBotComponent createComponent(String subdomain, String secret) throws ComponentException {
        checkNotNull(subdomain);
        checkNotNull(secret);

        WhackBotComponent component = new WhackBotComponent(this, subdomain);
        manager.setSecretKey(subdomain, secret);
        manager.setMultipleAllowed(subdomain, true);
        manager.removeComponent(subdomain);
        manager.addComponent(subdomain, component);
        component.setConnected(true);
        Log.info("Components Connected");

        return component;
    }

    private String getSubdomain(JID address) {
        return address.getDomain().substring(0, address.getDomain().indexOf("."));
    }

    private void verifyAddress(JID address) {
        // TODO: enforce address has subdomain and matches domain for this connector
    }

    private void removeComponent(WhackBotComponent component) throws ConnectorException {
        try {
            manager.removeComponent(component.getSubdomain());
        } catch (ComponentException e) {
            throw new ConnectorException("Exception while trying to remove component " + component, e);
        }
    }

    @Override
    public void doStart() throws ConnectorException {

    }

    @Override
    public void doStop() throws ConnectorException {

    }

    @Override
    public void doSend(Channel channel, Packet packet) throws ConnectorException {
        getConnection(channel).send(packet);
    }

    public void receiveFromComponent(Channel channel, Packet packet) throws ConnectorException {
        receive(channel, packet);
    }
}
