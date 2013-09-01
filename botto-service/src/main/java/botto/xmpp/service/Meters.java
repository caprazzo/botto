package botto.xmpp.service;

import botto.xmpp.botto.xmpp.connector.ConnectorId;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

public class Meters {

    public static final MetricRegistry Metrics = new MetricRegistry();

    private static String metricName(ConnectorId connectorId) {
        return ("#" + connectorId.getId() + "_" + connectorId.getClazz().getSimpleName() + "_" + connectorId.getName()).replaceAll("\\.", "_");
    }

    public static final ConnectorsMetrics connectors = new ConnectorsMetrics();

    public static final class ConnectorsMetrics {
        private final ConnectorMetrics allConnectors = new ConnectorMetrics(null, "all");

        public ConnectorMetrics forConnector(ConnectorId connectorId) {
            return new ConnectorMetrics(allConnectors, metricName(connectorId));
        }
    }

    public static final class ConnectorMetrics {

        private final PacketMetrics all;
        private final PacketMetrics message;
        private final PacketMetrics presence;
        private final PacketMetrics iq;
        private final PacketMetrics other;
        private final ConnectorMetrics allConnectors;

        private ConnectorMetrics(ConnectorMetrics allConnectors, String name) {
            this.allConnectors = allConnectors;
            all = new PacketMetrics(name, "all");
            message = new PacketMetrics(name, "message");
            presence = new PacketMetrics(name, "presence");
            iq = new PacketMetrics(name, "iq");
            other = new PacketMetrics(name, "other");
        }

        public void countIncoming(Packet packet) {
            countIncoming();

            if (packet instanceof Message) {
                countIncomingMessage();
            }
            else if (packet instanceof Presence) {
                countIncomingPresence();
            }
            else if (packet instanceof IQ) {
                countIncomingIQ();
            }
            else {
                countIncomingOther();
            }
        }

        private void countIncoming() {
            all.received.mark();
            if (allConnectors != null) {
                allConnectors.all.received.mark();
            }
        }

        private void countIncomingMessage() {
            message.received.mark();
            if (allConnectors != null) {
                allConnectors.message.received.mark();
            }
        }

        private void countIncomingPresence() {
            presence.received.mark();
            if (allConnectors != null) {
                allConnectors.presence.received.mark();
            }
        }

        private void countIncomingIQ() {
            iq.received.mark();
            if (allConnectors != null) {
                allConnectors.iq.received.mark();
            }
        }

        private void countIncomingOther() {
            other.received.mark();
            if (allConnectors != null) {
                allConnectors.other.received.mark();
            }
        }
    }

    private static final class PacketMetrics {

        public final Meter received;
        public final Meter sent;

        public PacketMetrics(String connector, String name) {
            received = Meters.Metrics.meter(MetricRegistry.name(Meters.class ,"connectors", connector, "packets", name, "received"));
            sent = Meters.Metrics.meter(MetricRegistry.name(Meters.class ,"connectors", connector, "packets", name, "sent"));
        }
    }

}
