package botto.xmpp;

import botto.xmpp.botto.xmpp.connector.Connector;
import botto.xmpp.botto.xmpp.connector.ConnectorId;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConnectorRegistryTest {

    @Rule
    public ExpectedException exex = ExpectedException.none();
    ConnectorRegistry registry = new ConnectorRegistry();

    @Test
    public void should_add_connector() {
        Connector connector = mock(Connector.class);
        ConnectorId connectorId = mock(ConnectorId.class);
        when(connector.getConnectorId()).thenReturn(connectorId);
        registry.addConnector(connector);
        Assert.assertEquals(connector, registry.getConnector(connectorId));
    }

    @Test
    public void should_remove_connector() {
        Connector connector = mock(Connector.class);
        ConnectorId connectorId = mock(ConnectorId.class);
        when(connector.getConnectorId()).thenReturn(connectorId);
        registry.addConnector(connector);
        Assert.assertEquals(connector, registry.removeConnector(connectorId));
    }

    @Test
    public void should_reject_connector_with_empty_id() {
        Connector connector = mock(Connector.class);
        exex.expect(BottoRuntimeException.class);
        exex.expectMessage(org.hamcrest.Matchers.startsWith("Could not register connector"));
        registry.addConnector(connector);
    }

    @Test
    public void should_reject_duplicate_connectors() {
        Connector connector = mock(Connector.class);
        ConnectorId connectorId = mock(ConnectorId.class);
        when(connector.getConnectorId()).thenReturn(connectorId);
        registry.addConnector(connector);
        exex.expect(BottoRuntimeException.class);
        exex.expectMessage(org.hamcrest.Matchers.startsWith("Could not register connector"));
        registry.addConnector(connector);
    }

    @Test
    public void should_throw_if_connector_not_found_on_get() {
        ConnectorId connectorId = mock(ConnectorId.class);
        exex.expect(BottoRuntimeException.class);
        exex.expectMessage(org.hamcrest.Matchers.startsWith("Could not find"));
        registry.getConnector(connectorId);
    }

    @Test
    public void should_throw_if_connector_not_found_on_remove() {
        ConnectorId connectorId = mock(ConnectorId.class);
        exex.expect(BottoRuntimeException.class);
        exex.expectMessage(org.hamcrest.Matchers.startsWith("Could not remove"));
        registry.removeConnector(connectorId);
    }

    @Test
    public void should_list_connectors() {
        Connector connectorA = mock(Connector.class);
        ConnectorId connectorIdA = mock(ConnectorId.class);
        when(connectorA.getConnectorId()).thenReturn(connectorIdA);

        Connector connectorB = mock(Connector.class);
        ConnectorId connectorIdB = mock(ConnectorId.class);
        when(connectorB.getConnectorId()).thenReturn(connectorIdB);

        registry.addConnector(connectorA);
        registry.addConnector(connectorB);

        assertTrue(registry.list().contains(connectorA));
        assertTrue(registry.list().contains(connectorB));
        assertEquals(2, registry.list().size());
    }
}
