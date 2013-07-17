package botto.xmpp.service.component;

import botto.xmpp.service.utils.QueueExecutor;
import com.google.common.base.Preconditions;
import org.jivesoftware.whack.ExternalComponentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.component.Component;
import org.xmpp.packet.Packet;

/**
 * Sends packets from xmpp components.
 * Incoming messages are queued up and sent asynchronously.
 */
public class ComponentPacketSender extends QueueExecutor<ComponentPacketSender.ComponentPacket> {

    private final Logger Log = LoggerFactory.getLogger(ComponentPacketSender.class);

    private final ExternalComponentManager manager;

    public ComponentPacketSender(ExternalComponentManager manager) {
        this.manager = manager;
    }

    @Override
    public void doProcess(ComponentPacket packet) {
        Log.debug("Sending response {}", packet.getPacket());
        if (packet.getPacket() == null) {
            Log.warn("Outgoing packet is null, not sending. {}", packet);
            return;
        }
        manager.sendPacket(packet.getComponent(), packet.getPacket());
    }

    public void send(Component component, Packet packet) {
        Preconditions.checkNotNull(component, "Component can't be null");
        Preconditions.checkNotNull(packet, "Packet can't be null");
        enqueue(new ComponentPacket(component, packet));
    }

    public static class ComponentPacket {
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
