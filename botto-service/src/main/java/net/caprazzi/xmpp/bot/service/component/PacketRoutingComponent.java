package net.caprazzi.xmpp.bot.service.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.Packet;

/**
 * An XMPP Component that captures all incoming packets for a subdomain and sends
 * them to a @see ComponentBotRouter.
 */
public class PacketRoutingComponent extends AbstractInterceptComponent {
    private final ComponentBotRouter router;
    private final Logger log;

    public PacketRoutingComponent(ComponentBotRouter router, String subdomain) {
        super(subdomain);
        log = LoggerFactory.getLogger(PacketRoutingComponent.class.getName() + "." + subdomain);
        this.router = router;
    }

    @Override
    public void processPacket(Packet packet) {
        log.debug("Processing incoming packet {}", packet.toXML());
        router.route(subdomain, packet);
    }
}
