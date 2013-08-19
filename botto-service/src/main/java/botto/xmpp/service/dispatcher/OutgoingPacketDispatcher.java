package botto.xmpp.service.dispatcher;

import botto.xmpp.annotations.PacketOutput;
import botto.xmpp.engine.BotConnection;
import botto.xmpp.service.Bot;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import net.caprazzi.reusables.common.Managed;
import net.caprazzi.reusables.threading.SingleThreadQueueExecutor;
import org.xmpp.packet.Packet;

public class OutgoingPacketDispatcher extends EnvelopeDispatcher<Bot, BotConnection> implements Managed {

    private final SingleThreadQueueExecutor<PacketEnvelope<PacketOutput>> outputQueue;

    public OutgoingPacketDispatcher() {
        outputQueue = new SingleThreadQueueExecutor<PacketEnvelope<PacketOutput>>() {
            @Override
            protected void doProcess(PacketEnvelope<PacketOutput> envelope) {
                try {
                    envelope.getLabel().send(envelope.getPacket());
                }
                catch(Exception ex) {
                    throw new RuntimeException("Error while sending " + envelope, ex);
                }
            }
        };
    }

    // enqueues incoming packets, to be sent in a separate thread
    protected ListenableConfirmation doDispatch(PacketOutput output, Packet packet) {
        return confirmFutureSuccess(outputQueue.enqueue(new PacketEnvelope<PacketOutput>(output, packet)));
    }

    private ListenableConfirmation confirmFutureSuccess(ListenableFuture future) {
        final ListenableConfirmation confirmation = ListenableConfirmation.create();
        Futures.addCallback(future, new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                confirmation.setSuccess();
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
        outputQueue.start();
    }

    @Override
    public void stop() {
        outputQueue.stop();
    }

}
