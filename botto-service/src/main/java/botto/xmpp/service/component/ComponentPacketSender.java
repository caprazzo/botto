package botto.xmpp.service.component;

import org.jivesoftware.whack.ExternalComponentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.component.Component;
import org.xmpp.packet.Packet;

import java.util.concurrent.*;

/**
 * Sends packets from xmpp components.
 * Incoming messages are queued up and sent asynchronously.
 */
public class ComponentPacketSender {

    private final Logger Log = LoggerFactory.getLogger(ComponentPacketSender.class);

    private final ExternalComponentManager manager;

    private BlockingQueue<ComponentPacket> outbox = new LinkedBlockingQueue<ComponentPacket>();

    final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ComponentPacketSender(ExternalComponentManager manager) {
        this.manager = manager;
    }

    public void start() {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                while(!Thread.currentThread().isInterrupted()) {
                    try {
                        ComponentPacket response = outbox.take();
                        Log.debug("Sending response {}", response.getPacket());
                        manager.sendPacket(response.getComponent(), response.getPacket());
                    } catch (InterruptedException e) {
                        // it's ok, we are shutting down now
                    }
                }
            }
        });
    }

    public void shutdown() {
        executor.shutdownNow();
        Log.info("Shut down complete");
    }

    public void send(Component component, Packet packet) {
        try {
            outbox.put(new ComponentPacket(component, packet));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static class ComponentPacket {
        private final Component component;
        private final Packet packet;

        public ComponentPacket(Component component, Packet packet) {
            this.component = component;
            this.packet = packet;
        }

        public Component getComponent() {
            return component;
        }

        public Packet getPacket() {
            return packet;
        }
    }
}
