package net.caprazzi.xmpp.bot.service;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import net.caprazzi.xmpp.bot.service.component.NodeFilter;
import net.caprazzi.xmpp.bot.service.component.NodeFilters;
import net.caprazzi.xmpp.bot.service.reflection.AnnotatedBotObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SubdomainEnvironment {

    private final String subdomain;
    private final ServiceEnvironment service;
    private String secret;
    private final Logger log;

    private final ArrayList<SubdomainBotEnvironment> bots = new ArrayList<SubdomainBotEnvironment>();

    SubdomainEnvironment(ServiceEnvironment service, String subdomain) {
        this.service = service;
        Preconditions.checkNotNull(subdomain, "Subdomain must not be null.");
        log = LoggerFactory.getLogger(this.getClass() + "." + subdomain);
        this.subdomain = subdomain;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public List<SubdomainBotEnvironment> getBots() {
        return bots;
    }

    public void addBot(Object bot, String node) {
        Preconditions.checkNotNull(bot, "Bot object must not be null.");
        Preconditions.checkNotNull(node, "node must not be null.");

        addBot(bot, NodeFilters.singleNode(node));
    }

    private void addBot(Object bot, NodeFilter nodeFilter) {
        Preconditions.checkNotNull(bot, "Bot object must not be null.");
        Preconditions.checkArgument(!service.isBotInOtherSubdomains(this, bot), "This bot instance has already been added to another subdomain. A bot instance can only be added to one subdomain.");
        Preconditions.checkNotNull(nodeFilter, "NodeFilter must not be null.");

        Optional<AnnotatedBotObject> annotatedBot = AnnotatedBotObject.from(bot);
        if (!annotatedBot.isPresent()) {
            log.error("Provided annotated bot is not a valid bot implementation: {}", bot);
            return;
        }
        bots.add(new SubdomainBotEnvironment(annotatedBot.get(), nodeFilter));
    }

    public String getName() {
        return subdomain;
    }

    public String getSecret() {
        return secret;
    }

    public boolean hasBot(Object bot) {
        for (SubdomainBotEnvironment botEnv : bots) {
            if (botEnv.getBot().getObject() == bot) {
                return true;
            }
        }
        return false;
    }
}
