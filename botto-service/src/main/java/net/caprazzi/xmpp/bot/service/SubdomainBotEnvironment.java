package net.caprazzi.xmpp.bot.service;

import net.caprazzi.xmpp.bot.service.component.NodeFilter;
import net.caprazzi.xmpp.bot.service.reflection.AnnotatedBotObject;

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
