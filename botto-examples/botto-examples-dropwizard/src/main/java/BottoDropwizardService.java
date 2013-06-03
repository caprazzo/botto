import botto.xmpp.service.AbstractBotService;
import botto.xmpp.service.ServiceConfiguration;
import botto.xmpp.service.ServiceEnvironment;
import botto.xmpp.service.SubdomainEnvironment;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;


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

        ServiceConfiguration botServiceConfiguration = new ServiceConfiguration();
        botServiceConfiguration.setHost(configuration.getXmppServer());
        botServiceConfiguration.setPort(configuration.getXmppServerPort());
        botServiceConfiguration.setSecret(configuration.getXmppServerComponentSecret());

        new AbstractBotService() {
            @Override
            protected void run(ServiceEnvironment environment) {
                SubdomainEnvironment subdomain = environment.getSubdomain("dropwizard");
                subdomain.addBot(bot, "hello");
            }
        }.run(botServiceConfiguration);
    }

    @Path("/user/{jid}/")
    public static class SayHelloResource {

        private final SayHelloBot helloBot;

        public SayHelloResource(SayHelloBot helloBot) {
            this.helloBot = helloBot;
        }

        @POST
        @Path("hello")
        public void sayHello(@PathParam("jid") String toJid) {
            helloBot.sayHello(toJid);
        }
    }

}
