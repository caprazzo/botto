package net.caprazzi.xmpp.bot.service.component;

import org.xmpp.component.Component;
import org.xmpp.component.ComponentException;
import org.xmpp.component.ComponentManager;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

/**
 *  An XMPP component that only captures packets
 */
public abstract class AbstractInterceptComponent implements Component {

    protected final String subdomain;

    public AbstractInterceptComponent(String subdomain) {
        this.subdomain = subdomain;
    }

    @Override
    public abstract void processPacket(Packet packet);

    @Override
    public String getName() {
        return "Bot Component Manager for subdomain " + subdomain;
    }

    @Override
    public String getDescription() {
        return getName();
    }

    @Override
    public void initialize(JID jid, ComponentManager componentManager) throws ComponentException {
        // nothing to do here
    }

    @Override
    public void start() {
        // nothing to do here
    }

    @Override
    public void shutdown() {
        // nothing to do here
    }
}
