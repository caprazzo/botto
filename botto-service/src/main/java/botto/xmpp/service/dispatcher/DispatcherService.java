package botto.xmpp.service.dispatcher;

import botto.xmpp.annotations.PacketOutput;
import botto.xmpp.engine.BotConnection;
import botto.xmpp.service.AbstractBot;
import botto.xmpp.service.Bot;
import botto.xmpp.service.BottoService;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
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

    public synchronized void removeConnection(AbstractBot bot, BotConnection connection) {
       // TODO: also use connection when removing
       incomingDispatcher.removeRoute(bot);
       outgoingDispatcher.removeRoute(bot);
    }

    public synchronized void addConnection(final AbstractBot bot, final BotConnection connection) {

        // TODO: this listener should probably be set on the connector, to
        // further simplify Connection

        // capture messages coming from this connection and put them to the incoming dispatcher
        connection.setConnectionPacketListener(new BotConnection.ConnectionPacketListener() {
            @Override
            public void onPacket(final Packet packet) {
                incomingDispatcher.dispatch(new PacketEnvelope<BotConnection>(connection, packet));
            }
        });

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
