package botto.xmpp.service.dispatcher;

import botto.xmpp.botto.xmpp.connector.BotConnection;
import botto.xmpp.service.Bot;
import botto.xmpp.service.Meters;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import net.caprazzi.reusables.common.Managed;
import net.caprazzi.reusables.threading.SingleThreadQueueExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

import java.util.UUID;

public class OutgoingPacketDispatcher extends EnvelopeDispatcher<Bot, BotConnection> implements Managed {

    private final static Logger Log = LoggerFactory.getLogger(OutgoingPacketDispatcher.class);

    private final SingleThreadQueueExecutor<PacketEnvelope<BotConnection>> outputQueue;

    public OutgoingPacketDispatcher() {
        outputQueue = new SingleThreadQueueExecutor<PacketEnvelope<BotConnection>>() {
            @Override
            protected void doProcess(PacketEnvelope<BotConnection> envelope) {
                try {
                    Log.debug("Sending {} to {}", envelope.getPacket(), envelope.getLabel());
                    envelope.getLabel().send(envelope.getPacket());
                }
                catch(Exception ex) {
                    throw new RuntimeException("Error while sending " + envelope, ex);
                }
            }
        };


        // TODO: can we put this inside the executor directly?
        Meters.Metrics.register(MetricRegistry.name(OutgoingPacketDispatcher.class, "depth"),
                new Gauge<Integer>() {
                    @Override
                    public Integer getValue() {
                        return outputQueue.size();
                    }
                });
    }

    @Override
    // enqueues incoming packets, to be sent in a separate thread
    protected ListenableConfirmation doDispatch(final BotConnection destination, final Packet packet) {
        Log.debug("Dispatching packet {} to connection {}", packet, destination);

        // TODO: here need to patch packet ID and From
        final Packet prepared = preparePacketForSending(packet, destination);

        final ListenableConfirmation confirmation = ListenableConfirmation.create();
        Futures.addCallback(outputQueue.enqueue(new PacketEnvelope<BotConnection>(destination, prepared)), new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                Log.debug("SUCCESS sending packet {} to {}", prepared, destination);
                confirmation.setSuccess();
            }

            @Override
            public void onFailure(Throwable failure) {
                Log.debug("ERROR sending packet {} to {}: {}", prepared, destination, failure);
                confirmation.setFailure(failure);
            }
        });

        return confirmation;
    }

    // TODO: should the logic be implemented in BotConnection ?
    // TODO: each connection should have a unique ID and an incrementor for packets

    private Packet preparePacketForSending(Packet packet, BotConnection destination) {
        // TODO: only clone the packet if changes are required
        Packet prepared = packet.createCopy();

        if (packet instanceof Message || packet instanceof IQ) {
            if (Strings.isNullOrEmpty(prepared.getID())) {
                prepared.setID(UUID.randomUUID().toString());
                Log.debug("Setting empty ID to {} for packet {}", prepared.getID(), packet);
            }
        }

        if (prepared.getFrom() == null) {
            Log.debug("Setting empty FROM to {} for packet {}", destination.getSendAddress(), prepared);
            prepared.setFrom(destination.getSendAddress());
        }

        return prepared;
    }


    @Override
    public void start() {
        outputQueue.start();
    }

    @Override
    public void stop() {
        outputQueue.stop();
    }
}
