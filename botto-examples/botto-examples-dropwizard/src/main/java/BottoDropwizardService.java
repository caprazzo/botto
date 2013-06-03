import botto.xmpp.service.AbstractBotService;
import botto.xmpp.service.ServiceEnvironment;
import botto.xmpp.service.SubdomainEnvironment;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;


public class BottoDropwizardService extends Service<BottoDropwizardServiceConfiguration> {

    public static void main(String[] args) throws Exception {
        new BottoDropwizardService().run(args);
    }

    @Override
    public void initialize(Bootstrap<BottoDropwizardServiceConfiguration> bootstrap) {
        bootstrap.setName("botto-dropwizard-integration");
    }

    @Override
    public void run(BottoDropwizardServiceConfiguration configuration, Environment environment) throws Exception {

        final SayHelloBot bot = new SayHelloBot();
        SayHelloResource resource = new SayHelloResource(bot);

        environment.addResource(resource);

        new AbstractBotService() {
            @Override
            protected void run(ServiceEnvironment environment) {
                SubdomainEnvironment subdomain = environment.getSubdomain("dropwizard");
                subdomain.addBot(bot, "hello");
            }
        }.run(configuration.getBotService());
    }

}
