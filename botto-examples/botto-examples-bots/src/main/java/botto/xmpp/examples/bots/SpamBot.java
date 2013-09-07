package botto.xmpp.examples.bots;

import botto.xmpp.annotations.Context;
import botto.xmpp.annotations.Receive;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SpamBot implements Runnable {

    //private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final JID dest;
    private int count = 0;

    @Context
    botto.xmpp.annotations.PacketOutput out;

    public SpamBot(JID dest) {
        this.dest = dest;
    }

    @Receive
    public void receive(Message message) {

    }

    @Override
    public void run() {
        Message message = new Message();
        message.setBody("Message #" + count);
        message.setTo(dest);
        out.send(message);
        count++;
    }
}
