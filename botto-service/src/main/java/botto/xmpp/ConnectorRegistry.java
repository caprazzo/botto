package botto.xmpp;

import botto.xmpp.botto.xmpp.connector.Connector;
import botto.xmpp.botto.xmpp.connector.ConnectorId;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectorRegistry {
    private final ConcurrentHashMap<ConnectorId, Connector> connectors = new ConcurrentHashMap<ConnectorId, Connector>();
    private final AtomicInteger connectorCount = new AtomicInteger();

    public ConnectorId addConnector(Connector connector) {
        if (connectors.containsValue(connector)) {
            throw new BottoRuntimeException("Could not register connector {} because it has already been registered", connector);
        }
        final ConnectorId connectorId = new ConnectorId(connectorCount.getAndIncrement(), connector.getClass(), connector.getName());
        connectors.put(connectorId, connector);
        return connectorId;
    }

    public Connector removeConnector(ConnectorId connectorId) {
        Connector removed = connectors.remove(connectorId);
        if (removed == null) {
            throw new BottoRuntimeException("Could not remove connector with ID {}: not found");
        }
        return removed;
    }

    public Connector getConnector(ConnectorId connectorId) {
        return connectors.get(connectorId);
    }

    public Collection<Connector> list() {
        return connectors.values();
    }
}
