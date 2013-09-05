package botto.xmpp.examples;

import botto.xmpp.annotations.ConnectionInfo;
import botto.xmpp.annotations.Context;
import botto.xmpp.annotations.PacketOutput;
import botto.xmpp.annotations.Receive;
import botto.xmpp.service.*;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;

public class EchoBotService {

    public static void main(String[] main) {
        BotServiceConfiguration configuration = new BotServiceConfiguration();
        configuration.setHost("localhost");
        configuration.setComponentPort(5275);
        configuration.setComponentSecret("secret");
        //new EchoBotService().run(configuration);
    }

    public void run(ServiceEnvironment environment) {
        EchoBot echoBot = new EchoBot();

        // setup echo bot to listen at echo@subdomain1.yourdomain.com
        SubdomainEnvironment subdomain = environment.getSubdomain("subdomain1");
        subdomain.addBot(echoBot, "echo");

        RelayBot relayBot = new RelayBot();

        // setup relay bot to listen at relay@subdomain2.yourdomain.com
        SubdomainEnvironment subdomain2 = environment.getSubdomain("subdomain2");
        subdomain2.addBot(relayBot, "relay");

        // setup relay bot to listen at echo@yourdomain.com
        // (you need to create a matching user in your xmpp server)
        BotEnvironment echoSingleBotEnv = environment.getBot("echo");
        echoSingleBotEnv.setBot(echoBot);
        echoSingleBotEnv.setSecret("secret");
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
        ConnectionInfo connectionInfo;

        @Context
        private PacketOutput output;

        @Receive
        public void Receive(Message msg) {
            Message relayOne = new Message();
            relayOne.setFrom("relay");
            relayOne.setTo(new JID("bigbrother@example.com"));
            relayOne.setBody(msg.getFrom() + " just said " + msg.getBody());

            output.send(relayOne);

            Message relayTwo = new Message();
            relayOne.setFrom("relay");
            relayTwo.setTo(new JID("bigsister@example.com"));
            relayTwo.setBody(msg.getFrom() + " just said " + msg.getBody());

            output.send(relayTwo);
        }
    }
}
