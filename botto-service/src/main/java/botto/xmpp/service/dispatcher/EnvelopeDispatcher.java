package botto.xmpp.service.dispatcher;

import botto.xmpp.Meters;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class EnvelopeDispatcher<TLabel, TTarget> {

    private final Logger Log;
    private final Timer mDispatch;

    public EnvelopeDispatcher() {
        Log = LoggerFactory.getLogger(this.getClass());
        mDispatch = Meters.Metrics.timer(MetricRegistry.name(this.getClass(), "packets", "processing"));
    }

    protected abstract Timer getRoutingTimer();

    protected abstract ListenableConfirmation doDispatch(TTarget destination, Packet packet);

    public ListenableConfirmation dispatch(PacketEnvelope<TLabel> envelope) {
        // this timer measures how long it takes to process a packet:
        // for incoming packets, this includes adding and getting from the incoming queue,
        // processing in the bot and sending the response if one is generated
        // for outgoing packets, this includes only the time spent in the queue and the time to send it
        final Timer.Context time = mDispatch.time();
        Timer.Context routingTime = getRoutingTimer().time();
        TTarget route = table.get(getRoutingAddress(envelope.getPacket()));

        routingTime.stop();

        if (route == null) {
            Log.warn("No destination found for {}", envelope);
            time.stop();
            return ListenableConfirmation.failed(new RuntimeException("Could not route packet to any destination"));
        }

        ListenableConfirmation confirmation = doDispatch(route, envelope.getPacket());
        Futures.addCallback(confirmation, new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                // TODO: log
                time.stop();
            }

            @Override
            public void onFailure(Throwable t) {
                // TODO: log
                time.stop();
            }
        });

        return confirmation;
    }

    protected abstract JID getRoutingAddress(Packet packet);

    private final Map<JID, TTarget> table = new ConcurrentHashMap<JID, TTarget>();
    public final void addRoute(JID address, TTarget target) {
        Log.info("Adding route {} to {}", address, target);
        table.put(address, target);
    }

    public final void removeRoute(JID address) {
        Log.info("Removing route to {}", address);
        table.remove(address);
    }
}
