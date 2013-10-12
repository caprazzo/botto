package botto.xmpp;

import botto.xmpp.botto.xmpp.connector.Connector;
import botto.xmpp.botto.xmpp.connector.ConnectorId;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectorRegistry {
    private final ConcurrentHashMap<ConnectorId, Connector> connectors = new ConcurrentHashMap<ConnectorId, Connector>();

    public ConnectorId addConnector(Connector connector) {
        if (connectors.containsValue(connector)) {
            throw new BottoRuntimeException("Could not register connector {0} because it has already been registered", connector);
        }
        if (connector.getConnectorId() == null) {
            throw new BottoRuntimeException("Could not register connector {0} because it has null ID", connector);
        }
        connectors.put(connector.getConnectorId(), connector);
        return connector.getConnectorId();
    }

    public Connector removeConnector(ConnectorId connectorId) {
        Connector removed = connectors.remove(connectorId);
        if (removed == null) {
            throw new BottoRuntimeException("Could not remove connector with ID {0}: not found", connectorId);
        }
        return removed;
    }

    public Connector getConnector(ConnectorId connectorId) {
        Connector connector = connectors.get(connectorId);
        if (connector == null) {
            throw new BottoRuntimeException("Could not find connector for id {0}", connectorId);
        }
        return connector;
    }

    public Collection<Connector> list() {
        return connectors.values();
    }
}
