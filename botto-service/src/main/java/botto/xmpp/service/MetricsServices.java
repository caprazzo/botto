package botto.xmpp.service;

import botto.xmpp.botto.xmpp.connector.ConnectorId;
import com.codahale.metrics.MetricRegistry;

public class MetricsServices {
    public static final MetricRegistry Metrics = new MetricRegistry();
    public static String connectorPacketsAllIncoming(ConnectorId id) {
        return "network.connectors." + id.toString().replaceAll("\\.", "_") + ".packets.all.received";
    }
}
