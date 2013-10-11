package botto.xmpp.examples.bots;

import botto.xmpp.annotations.BotContext;
import botto.xmpp.annotations.Context;
import botto.xmpp.annotations.Receive;
import org.xmpp.packet.Message;

public class EchoBot {

    @Context BotContext botcontext;

    @Receive
    public Message receive(Message message) {
        //System.out.println("Received message " + message.getID());
        Message response = new Message();
        response.setBody("you fool just said: " + message.getBody());

        response.setTo(message.getFrom());
        //out.send(response);
        return response;
    }

    @Override
    public String toString() {
        return "ExampleBot";
    }
}
