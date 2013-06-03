import botto.xmpp.service.BotServiceConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;

/**
 * To configure a BotService from a Dropwizard configuration,
 * simply add BotServiceConfiguration as JsonProperty
 *
 * See botto-dropwizard-service.yml for how to add values to the configuration
 */
public class BottoDropwizardServiceConfiguration extends Configuration {

    @JsonProperty
    private BotServiceConfiguration botService = new BotServiceConfiguration();

    public BotServiceConfiguration getBotService( ){
        return botService;
    }
}
