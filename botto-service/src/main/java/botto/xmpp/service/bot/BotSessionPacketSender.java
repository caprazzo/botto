package botto.xmpp.service.bot;

import net.caprazzi.reusables.threading.SingleThreadQueueExecutor;
import org.jivesoftware.smack.packet.Packet;

/**
 * Async packet sender.
 * TODO: don't dequeue if the bot is not available
 */
public class BotSessionPacketSender extends SingleThreadQueueExecutor<BotSessionPacket> {

    protected BotSessionPacketSender() {
        super("bot-session-packet-sender");
    }

    @Override
    public void doProcess(BotSessionPacket packet) {
       //TODO: handle the return value of sendPacket
       packet.getSession().sendPacket(packet.getPacket());
    }

    public void send(BotSession connection, Packet packet) {
        // TODO: could this return a Future with the result of the send operation?
        enqueue(new BotSessionPacket(connection, packet));
    }

}
