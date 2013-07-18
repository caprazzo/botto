package botto.xmpp.service;

import botto.xmpp.service.reflection.AnnotatedBotObject;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BotEnvironment {

    private final Logger log;

    private final String node;
    private AnnotatedBotObject bot;
    private String secret;
    private String resource;

    public BotEnvironment(String node) {
        this.node = node;
        Preconditions.checkNotNull(node, "Bot id must not be null.");
        log = LoggerFactory.getLogger(this.getClass() + "." + node);
    }

    public void setBot(Object bot) {
        Optional<AnnotatedBotObject> annotatedBot = AnnotatedBotObject.from(bot);
        if (!annotatedBot.isPresent()) {
            log.error("Provided annotated bot is not a valid bot implementation: {}", bot);
            return;
        }
        this.bot = annotatedBot.get();
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getNode() {
        return node;
    }

    public AnnotatedBotObject getBot() {
        return bot;
    }

    public String getSecret() {
        return secret;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getResource() {
        return resource;
    }
}
