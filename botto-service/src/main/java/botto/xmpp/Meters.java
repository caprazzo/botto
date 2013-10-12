package botto.xmpp;

import botto.xmpp.botto.xmpp.connector.ConnectorId;
import com.codahale.metrics.*;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.*;

public class Meters {

    public static final MetricRegistry Metrics = new MetricRegistry();

    private static String metricName(ConnectorId connectorId) {
        return connectorId.toString();
    }

    public static final ConnectorsMetrics connectors = new ConnectorsMetrics();

    public static final Timer incomingRoutingTimer = Meters.Metrics.timer(name(Meters.class, "engine", "routing", "incoming"));
    public static final Timer outgoingRoutingTimer = Meters.Metrics.timer(name(Meters.class, "engine", "routing", "outgoing"));

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
        private final Counter channels;
        private final Timer delivery;
        private final Meter deliveryError;
        private final Meter response;

        private ConnectorMetrics(ConnectorMetrics allConnectors, String name) {
            this.allConnectors = allConnectors;
            channels = Meters.Metrics.counter(name(Meters.class, "connectors", name, "channels", "open"));
            delivery = Meters.Metrics.timer(name(Meters.class, "connectors", name, "bot", "delivery", "attempt"));
            deliveryError = Meters.Metrics.meter(name(Meters.class, "connectors", name, "bot", "delivery", "error"));
            try {
                Meters.Metrics.register(name(Meters.class, "connectors", name, "bot", "delivery", "ratio"), new DeliverySuccessRatio(delivery, deliveryError));
            }
            catch (Exception ex) {
                // TODO: this nly happens if metrics are created for multiple connectors with the same name.
                //ex.printStackTrace();
            }
            response = Meters.Metrics.meter(name(Meters.class, "connectors", name, "bot", "response"));
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

        public void countOutgoing(Packet packet) {
            countOutgoing();

            if (packet instanceof Message) {
                countOutgoingMessage();
            }
            else if (packet instanceof Presence) {
                countOutgoingPresence();
            }
            else if (packet instanceof IQ) {
                countOutgoingIQ();
            }
            else {
                countOutgoingOther();
            }
        }

        private void countOutgoing() {
            all.sent.mark();
            if (allConnectors != null) {
                allConnectors.all.sent.mark();
            }
        }

        private void countOutgoingMessage() {
            message.sent.mark();
            if (allConnectors != null) {
                allConnectors.message.sent.mark();
            }
        }

        private void countOutgoingPresence() {
            presence.sent.mark();
            if (allConnectors != null) {
                allConnectors.presence.sent.mark();
            }
        }

        private void countOutgoingIQ() {
            iq.sent.mark();
            if (allConnectors != null) {
                allConnectors.iq.sent.mark();
            }
        }

        private void countOutgoingOther() {
            other.sent.mark();
            if (allConnectors != null) {
                allConnectors.other.sent.mark();
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

        public void countOpenChannel() {
            channels.inc();
            if (allConnectors != null) {
                allConnectors.channels.inc();
            }
        }

        public void countClosedChannel() {
            channels.dec();
            if (allConnectors != null) {
                allConnectors.channels.dec();
            }
        }

        public long startBotDelivery() {
            return System.nanoTime();
        }

        public void timeBotDelivery(long start) {
            long duration = System.nanoTime() - start;
            delivery.update(duration, TimeUnit.NANOSECONDS);
            if (allConnectors != null) {
                allConnectors.delivery.update(duration, TimeUnit.NANOSECONDS);
            }
        }

        public void countDeliveryError() {
            deliveryError.mark();
            if (allConnectors != null) {
                allConnectors.deliveryError.mark();
            }
        }

        public void countBotResponse() {
            response.mark();
            if (allConnectors != null) {
                allConnectors.response.mark();
            }
        }

        private static class DeliverySuccessRatio extends RatioGauge {

            private final Timer attempts;
            private final Meter failed;

            public DeliverySuccessRatio(Timer attempts, Meter failed) {
                this.attempts = attempts;
                this.failed = failed;
            }

            @Override
            protected Ratio getRatio() {
                return Ratio.of(attempts.getOneMinuteRate(), failed.getOneMinuteRate());
            }
        }
    }

    private static final class PacketMetrics {

        public final Meter received;
        public final Meter sent;

        public PacketMetrics(String connector, String name) {
            received = Meters.Metrics.meter(name(Meters.class, "connectors", connector, "packets", name, "received"));
            sent = Meters.Metrics.meter(name(Meters.class, "connectors", connector, "packets", name, "sent"));
        }
    }

}
