package botto.xmpp.service;

import botto.xmpp.service.component.NodeFilter;
import botto.xmpp.service.reflection.AnnotatedBotObject;
import org.xmpp.packet.JID;

public class SubdomainBotEnvironment {
    private final AnnotatedBotObject bot;
    private final NodeFilter nodeFilter;
    private final String node;

    public SubdomainBotEnvironment(AnnotatedBotObject bot, NodeFilter nodeFilter, String node) {
        this.bot = bot;
        this.nodeFilter = nodeFilter;
        this.node = node;
    }

    public AnnotatedBotObject getBot() {
        return bot;
    }

    public NodeFilter getNodeFilter() {
        return nodeFilter;
    }

    public void shutdown() {
        bot.shutdown();
    }

    public String getNode() {
        return node;
    }
}
