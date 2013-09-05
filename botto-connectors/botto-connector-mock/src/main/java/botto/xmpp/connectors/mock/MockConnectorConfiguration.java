package botto.xmpp.connectors.mock;

import botto.xmpp.botto.xmpp.connector.ConnectorConfiguration;

public class MockConnectorConfiguration implements ConnectorConfiguration {

    private final String name;

    public MockConnectorConfiguration(String name) {
        this.name = name;
    }

    private String domain;

    @Override
    public String getName() {
        return name;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }

}
