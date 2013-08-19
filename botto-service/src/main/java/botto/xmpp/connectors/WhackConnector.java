package botto.xmpp.connectors;

import botto.xmpp.engine.BotConnection;
import botto.xmpp.engine.Connector;
import botto.xmpp.engine.ConnectorException;
import botto.xmpp.service.AbstractBot;
import botto.xmpp.service.component.AbstractInterceptComponent;
import com.google.common.base.Preconditions;
import org.jivesoftware.whack.ExternalComponentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.component.ComponentException;
import org.xmpp.packet.JID;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkNotNull;

public class WhackConnector implements Connector {

    private static final Logger Log = LoggerFactory.getLogger(WhackConnector.class);

    private final ExternalComponentManager manager;
    private final WhackConnectorConfiguration configuration;

    private Map<String, WhackBotComponent> components = new ConcurrentHashMap<String, WhackBotComponent>();

    public WhackConnector(WhackConnectorConfiguration configuration) {
        this.configuration = configuration;
        // TODO the connector should be configured with host, port, domain, secret and a map subdomain -> password
        manager = new ExternalComponentManager(configuration.getHost(), configuration.getPort());
    }

    @Override
    public BotConnection createConnection(AbstractBot bot, JID address) throws ConnectorException {
        checkNotNull(bot, "A bot must not be null");
        checkNotNull(address, "The address must not be null");
        verifyAddress(address);
        // TODO: enforce address has subdomain and matches domain for this connector

        // get subdomain of bot
        String subdomain = getSubdomain(address);
        WhackBotComponent component = components.get(subdomain);
        if (component == null) {
            String secret;
            try {
                component = createComponent(subdomain, configuration.getSecret(subdomain));
            } catch (ComponentException e) {
                throw new ConnectorException("Error while creating component for subdomain " + subdomain, e);
            }

        }

        WhackBotConnection connection = new WhackBotConnection(component, address);
        component.addConnection(connection);
        return connection;
    }

    private WhackBotComponent createComponent(String subdomain, String secret) throws ComponentException {
        WhackBotComponent component = new WhackBotComponent(subdomain);
        manager.setSecretKey(subdomain, secret);
        manager.setMultipleAllowed(subdomain, true);
        manager.removeComponent(subdomain);
        manager.addComponent(subdomain, component);
        component.setConnected(true);
        Log.info("Components Connected");

        return component;
    }

    private String getSubdomain(JID address) {
        return address.getDomain().substring(0, address.getDomain().lastIndexOf("."));
    }

    private void verifyAddress(JID address) {
        // TODO: must have a domain in the form subdomain.<domain>
    }

    @Override
    public void removeConnection(BotConnection connection) throws ConnectorException {
        if (!(connection instanceof WhackBotConnection)) {
            throw new ConnectorException(new IllegalArgumentException("Can only remove connections of type WhackBotConection"));
        }

        for(WhackBotComponent component : components.values()) {
            component.removeConnection((WhackBotConnection)connection);
            if (component.isEmpty()) {
                removeComponent(component);
            }
            return;
        }
    }

    private void removeComponent(WhackBotComponent component) throws ConnectorException {
        try {
            manager.removeComponent(component.getSubdomain());
        } catch (ComponentException e) {
            throw new ConnectorException("Exception while trying to remove component " + component, e);
        }
    }

    public static class WhackConnectorConfiguration {
        private int port;
        private String host;
        private String domain;

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public String getSecret(String subdomain) {
            return null;  //To change body of created methods use File | Settings | File Templates.
        }
    }
}
