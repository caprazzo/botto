package botto.xmpp.service.dispatcher;

import botto.xmpp.AbstractBot;
import botto.xmpp.Bot;
import botto.xmpp.annotations.PacketOutput;
import botto.xmpp.botto.xmpp.connector.BotConnection;
import net.caprazzi.reusables.common.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.Packet;

public class DispatcherService implements Managed {

    private final static Logger Log = LoggerFactory.getLogger(DispatcherService.class);

    private final IncomingPacketDispatcher incomingDispatcher;
    private final OutgoingPacketDispatcher outgoingDispatcher;

    public DispatcherService() {
        outgoingDispatcher = new OutgoingPacketDispatcher();
        incomingDispatcher = new IncomingPacketDispatcher(outgoingDispatcher);
    }

    public void receive(BotConnection connection, Packet packet) {
        incomingDispatcher.dispatch(new PacketEnvelope<BotConnection>(connection, packet));
    }

    public synchronized void removeConnection(AbstractBot bot, BotConnection connection) {
       // TODO: also use connection when removing
       incomingDispatcher.removeRoute(bot);
       outgoingDispatcher.removeRoute(bot);
    }

    // TODO: return a ConnectionId
    public synchronized void addConnection(final AbstractBot bot, final BotConnection connection) {

        // route incoming packets to this bot.
        // TODO: can this be done with a table instead of scanning all filters?
        incomingDispatcher.addRoute(new PacketEnvelopeFilter<BotConnection>() {
            @Override
            public boolean accept(PacketEnvelope<BotConnection> packet) {
                return packet.getLabel().equals(connection);
            }
        }, bot);

        // route relevant outgoing packets to this connection
        outgoingDispatcher.addRoute(new PacketEnvelopeFilter<Bot>() {
            @Override
            public boolean accept(PacketEnvelope<Bot> packet) {
                return packet.getLabel().equals(bot);
            }
        }, connection);

        // set explicit output of this bot
        // TODO: this could be done in the caller of addConnection
        bot.setPacketOutput(new PacketOutput() {
            @Override
            public void send(Packet packet) {
                outgoingDispatcher.dispatch(new PacketEnvelope<Bot>(bot, packet));
            }
        });
    }

    @Override
    public void start() {
        Log.info("Starting");
        outgoingDispatcher.start();
        incomingDispatcher.start();
        Log.info("Started");
    }

    @Override
    public void stop() {
        Log.info("Shutdown starting");
        incomingDispatcher.stop();
        outgoingDispatcher.stop();
        Log.info("Shutdown complete");
    }


}
