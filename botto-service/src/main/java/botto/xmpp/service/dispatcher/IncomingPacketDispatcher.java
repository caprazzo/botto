package botto.xmpp.service.dispatcher;

import botto.xmpp.Bot;
import botto.xmpp.botto.xmpp.connector.BotConnection;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;

import com.google.common.util.concurrent.*;
import net.caprazzi.reusables.common.Managed;
import net.caprazzi.reusables.threading.SingleThreadQueueResultExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.Packet;

import com.codahale.metrics.Timer;

public class IncomingPacketDispatcher extends EnvelopeDispatcher<BotConnection, Bot> implements Managed {

    private final static Timer timer = Meters.Metrics.timer(MetricRegistry.name(IncomingPacketDispatcher.class, "bot", "receive"));

    private final static Logger Log = LoggerFactory.getLogger(IncomingPacketDispatcher.class);

    private final OutgoingPacketDispatcher output;

    private SingleThreadQueueResultExecutor<PacketEnvelope<Bot>, Optional<Packet>> dispatcherExecutor;

    public IncomingPacketDispatcher(OutgoingPacketDispatcher output) {
        this.output = output;
        dispatcherExecutor = new SingleThreadQueueResultExecutor<PacketEnvelope<Bot>, Optional<Packet>>() {
            @Override
            public Optional<Packet> doProcess(PacketEnvelope<Bot> envelope) {
                Log.debug("Processing {}", envelope);
                Bot bot = envelope.getLabel();
                Packet packet = envelope.getPacket();
                Timer.Context time = timer.time();
                try {
                    return Optional.fromNullable(bot.receive(packet));
                }
                finally {
                    time.stop();
                }
            }
        };

        // TODO: can we put this inside the executor directly?
        Meters.Metrics.register(MetricRegistry.name(IncomingPacketDispatcher.class, "depth"),
            new Gauge<Integer>() {
                @Override
                public Integer getValue() {
                    return dispatcherExecutor.size();
                }
            });
    }

    // TODO: should dispatch also return a future, so full tracing can be enabled?
    protected ListenableConfirmation doDispatch(final Bot bot, final Packet packet) {
        final ListenableConfirmation confirmation = ListenableConfirmation.create();
        final PacketEnvelope<Bot> envelope = new PacketEnvelope<Bot>(bot, packet);
        Futures.addCallback(dispatcherExecutor.enqueue(envelope), new FutureCallback<Optional<Packet>>() {
            @Override
            public void onSuccess(Optional<Packet> response) {

                Log.debug("Received response from {}: {}", envelope, response.orNull());

                if (!response.isPresent()) {
                    confirmation.setSuccess();
                    return;
                }

                // if there is a response, send it to the output dispatcher and wait for the result
                Futures.addCallback(output.dispatch(new PacketEnvelope<Bot>(bot, response.get())), new FutureCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        confirmation.setSuccess();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.error("Error while delivering {}", envelope);
                        confirmation.setFailure(throwable);
                    }
                });
            }

            @Override
            public void onFailure(Throwable throwable) {
                confirmation.setFailure(throwable);
            }
        });

        return confirmation;
    }

    @Override
    public void start() {
        dispatcherExecutor.start();
    }

    @Override
    public void stop() {
        dispatcherExecutor.stop();
    }
}
