package botto.xmpp.service.bot;

import com.google.common.util.concurrent.ListenableFuture;
import net.caprazzi.reusables.threading.SingleThreadQueueExecutor;
import org.jivesoftware.smack.packet.Packet;

/**
 * Async packet sender.
 * TODO: don't dequeue if the bot is not available
 */
public class BotSessionPacketSender extends SingleThreadQueueExecutor<BotSessionPacket> {

    @Override
    public void doProcess(BotSessionPacket packet) {
       //TODO: handle the return value of sendPacket
       packet.getSession().sendPacket(packet.getPacket());
    }

    public ListenableFuture send(BotSession connection, Packet packet) {
        // TODO: could this return a Future with the result of the send operation?
        return enqueue(new BotSessionPacket(connection, packet));
    }

}
