package net.caprazzi.xmpp;

import botto.xmpp.annotations.Receive;
import botto.xmpp.service.AbstractBotService;
import botto.xmpp.service.BotServiceConfiguration;
import botto.xmpp.service.ServiceEnvironment;
import botto.xmpp.service.SubdomainEnvironment;
import org.xmpp.packet.Message;

public class EchoBotService extends AbstractBotService {

    public static void main(String[] main) {
        BotServiceConfiguration configuration = new BotServiceConfiguration();
        configuration.setHost("localhost");
        configuration.setPort(5275);
        configuration.setSecret("secret");
        new EchoBotService().run(configuration);
    }

    @Override
    public void run(ServiceEnvironment environment) {
        EchoBot bot = new EchoBot();

        SubdomainEnvironment subdomain = environment.getSubdomain("foo");
        subdomain.addBot(bot, "echo");
        subdomain.addBot(bot, "echo2");

        SubdomainEnvironment subdomain2 = environment.getSubdomain("foo2");
        subdomain2.addBot(bot, "echo");
        subdomain2.addBot(bot, "echo2");
    }

    public static class EchoBot {
        @Receive
        public Message echo(Message msg) {
            Message reply = new Message();
            reply.setTo(msg.getFrom());
            reply.setFrom(msg.getTo());
            reply.setBody("You said xx: " + msg.getBody());
            return reply;
        }
    }
}
