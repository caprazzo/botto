package botto.xmpp.connectors.mock;

import botto.xmpp.engine.ConnectorConfiguration;

public class MockConnectorConfiguration implements ConnectorConfiguration {
    private String domain;

    @Override
    public String getName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }
}
