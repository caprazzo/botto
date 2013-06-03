import botto.xmpp.service.BotServiceConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;

public class BottoDropwizardServiceConfiguration extends Configuration {

    @JsonProperty
    private BotServiceConfiguration botService = new BotServiceConfiguration();

    public BotServiceConfiguration getBotService( ){
        return botService;
    }
}
