package botto.xmpp.examples.bots;

import botto.xmpp.annotations.Context;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;

public class SpamBot implements Runnable {

    private final JID dest;
    private int count = 0;

    @Context
    botto.xmpp.annotations.PacketOutput out;

    public SpamBot(JID dest) {
        this.dest = dest;
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
