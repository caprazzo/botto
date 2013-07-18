package botto.xmpp.service.bot;

import botto.xmpp.service.utils.QueueExecutor;
import org.jivesoftware.smack.packet.Packet;

/**
 * Async packet sender.
 * TODO: don't dequeue if the bot is not available
 */
public class BotSessionPacketSender extends QueueExecutor<BotSessionPacket> {
    @Override
    public void doProcess(BotSessionPacket packet) {
       //TODO: handle the return value of sendPacket
       packet.getSession().sendPacket(packet.getPacket());
    }

    public void send(BotSession connection, Packet packet) {
        enqueue(new BotSessionPacket(connection, packet));
    }

}
