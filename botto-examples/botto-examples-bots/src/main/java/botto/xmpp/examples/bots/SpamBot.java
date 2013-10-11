package botto.xmpp.examples.bots;

import botto.xmpp.annotations.BotContext;
import botto.xmpp.annotations.Context;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;

public class SpamBot implements Runnable {

    private final JID dest;
    private int count = 0;

    @Context
    BotContext context;

    public SpamBot(JID dest) {
        this.dest = dest;
    }

    @Override
    public void run() {
        if (context == null || !context.isConnected()) {
            System.err.println("Context status: " + context);
            return;
        }
        Message message = new Message();
        message.setBody("Message #" + count);
        message.setTo(dest);
        context.send(message);
        count++;
    }
}
