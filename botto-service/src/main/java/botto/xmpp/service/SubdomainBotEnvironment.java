package botto.xmpp.service;

import botto.xmpp.service.component.NodeFilter;
import botto.xmpp.service.reflection.AnnotatedBotObject;

class SubdomainBotEnvironment {
    private final AnnotatedBotObject bot;
    private final NodeFilter nodeFilter;

    public SubdomainBotEnvironment(AnnotatedBotObject bot, NodeFilter nodeFilter) {
        this.bot = bot;
        this.nodeFilter = nodeFilter;
    }

    public AnnotatedBotObject getBot() {
        return bot;
    }

    public NodeFilter getNodeFilter() {
        return nodeFilter;
    }
}
