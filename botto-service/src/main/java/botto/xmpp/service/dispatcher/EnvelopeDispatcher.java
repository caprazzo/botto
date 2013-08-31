package botto.xmpp.service.dispatcher;

import botto.xmpp.service.Bot;
import botto.xmpp.service.BottoService;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.Packet;

public abstract class EnvelopeDispatcher<TLabel, TTarget> {

    private final EnvelopeRouter<TLabel, TTarget> router = new EnvelopeRouter<TLabel, TTarget>();
    private final Logger Log;
    private final Meter mDispatch;

    public EnvelopeDispatcher() {
        Log = LoggerFactory.getLogger(this.getClass());
        mDispatch = BottoService.Metrics.meter(MetricRegistry.name(this.getClass(), "packets", "dispatch"));
    }

    protected abstract ListenableConfirmation doDispatch(TTarget destination, Packet packet);

    public ListenableConfirmation dispatch(PacketEnvelope<TLabel> envelope) {
        Optional<TTarget> route = router.route(envelope);
        if (!route.isPresent()) {
            Log.warn("No destination found for {}", envelope);
            return ListenableConfirmation.failed(new RuntimeException("Could not route packet to any destination"));
        }
        mDispatch.mark();
        return doDispatch(route.get(), envelope.getPacket());
    }

    public final void addRoute(PacketEnvelopeFilter<TLabel> filter, TTarget target) {
        Log.info("Adding route filter {} to {}", filter, target);
        router.addRoute(filter, target);
    }

    public final void removeRoute(Bot bot) {
        Log.info("Removing route to bot {}", bot);
        router.removeRoute(bot);
    }
}
