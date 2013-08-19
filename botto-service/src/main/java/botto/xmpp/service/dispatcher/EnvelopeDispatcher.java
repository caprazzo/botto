package botto.xmpp.service.dispatcher;

import botto.xmpp.service.Bot;
import com.google.common.base.Optional;
import org.xmpp.packet.Packet;

public abstract class EnvelopeDispatcher<TLabel, TTarget> {

    private final EnvelopeRouter<TLabel, TTarget> router = new EnvelopeRouter<TLabel, TTarget>();

    protected abstract ListenableConfirmation doDispatch(TTarget destination, Packet packet);

    public ListenableConfirmation dispatch(PacketEnvelope<TLabel> envelope) {
        Optional<TTarget> route = router.route(envelope);
        if (!route.isPresent()) {
            // TODO: improve explanation
            return ListenableConfirmation.failed(new RuntimeException("Could not route packet to any destination"));
        }
        return doDispatch(route.get(), envelope.getPacket());
    }

    public final void addRoute(PacketEnvelopeFilter<TLabel> filter, TTarget target) {
        router.addRoute(filter, target);
    }

    public final void removeRoute(Bot bot) {
        router.removeRoute(bot);
    }
}
