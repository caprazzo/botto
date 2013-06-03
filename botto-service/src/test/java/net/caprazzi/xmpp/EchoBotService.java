package net.caprazzi.xmpp;

import botto.xmpp.annotations.Context;
import botto.xmpp.annotations.PacketOutput;
import botto.xmpp.annotations.Receive;
import botto.xmpp.service.AbstractBotService;
import botto.xmpp.service.BotServiceConfiguration;
import botto.xmpp.service.ServiceEnvironment;
import botto.xmpp.service.SubdomainEnvironment;
import org.xmpp.packet.JID;
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
        EchoBot echoBot = new EchoBot();

        // setup echo bot to listen at echo@subdomain1.yourdomain.com
        SubdomainEnvironment subdomain = environment.getSubdomain("subdomain1");
        subdomain.addBot(echoBot, "echo");

        RelayBot relayBot = new RelayBot();

        // setup echo bot to listen at relay@subdomain2.yourdomain.com
        SubdomainEnvironment subdomain2 = environment.getSubdomain("subdomain2");
        subdomain2.addBot(relayBot, "relay");
    }

    /**
     * Simple bot echoes back any received message
     */
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

    /**
     * Simple bot that relays any received message
     * to multiple addresses
     */
    public static class RelayBot {

        @Context
        private PacketOutput output;

        @Receive
        public void Receive(Message msg) {
            Message relayOne = new Message();
            relayOne.setTo(new JID("bigbrother@example.com"));
            relayOne.setBody(msg.getFrom() + " just said " + msg.getBody());

            output.send(relayOne);

            Message relayTwo = new Message();
            relayTwo.setTo(new JID("bigsister@example.com"));
            relayTwo.setBody(msg.getFrom() + " just said " + msg.getBody());

            output.send(relayTwo);
        }
    }
}
