package botto.xmpp.service.dispatcher;

import botto.xmpp.annotations.PacketOutput;
import botto.xmpp.service.AbstractBot;
import botto.xmpp.service.Bot;
import botto.xmpp.service.bot.BotSessionManager;
import botto.xmpp.service.bot.PacketInputOutput;
import botto.xmpp.service.component.NodeFilter;
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

    public void addComponentBot(final AbstractBot bot, final NodeFilter nodeFilter, final PacketSource source, PacketOutput out) {
        incomingDispatcher.addRoute(new PacketEnvelopeFilter<PacketSource>() {
            @Override
            public boolean accept(PacketEnvelope<PacketSource> envelope) {
                return (envelope.getLabel().equals(source)
                    && nodeFilter.accept(envelope.getPacket().getTo().getNode()));
            }
        }, bot);

        outgoingDispatcher.addRoute(new PacketEnvelopeFilter<Bot>() {
            @Override
            public boolean accept(PacketEnvelope<Bot> packet) {
                return packet.getLabel().equals(bot);
            }
        }, out);

        // explicit packet output given to bots for sending via output.send
        bot.setPacketOutput(new PacketOutput() {
            @Override
            public void send(Packet packet) {
                outgoingDispatcher.dispatch(new PacketEnvelope<Bot>(bot, packet));
            }
        });
    }

    public void removeBot(Bot bot) {
        incomingDispatcher.removeRoute(bot);
        outgoingDispatcher.removeRoute(bot);
    }

    public void addNodeBot(final AbstractBot bot, final PacketSource source, final PacketOutput output) {

        // pick messages from source and send them to the incoming dispatcher
        source.setPacketSourceListener(new PacketSource.PacketSourceListener() {
            @Override
            public void receive(Packet packet) {
                incomingDispatcher.dispatch(new PacketEnvelope<PacketSource>(source, packet));
            }
        });

        // setup incoming messages to be routed to this bot
        incomingDispatcher.addRoute(new PacketEnvelopeFilter<PacketSource>() {
            @Override
            public boolean accept(PacketEnvelope<PacketSource> packet) {
                return packet.getLabel().equals(source);
            }
        }, bot);

        // setup outgoing messages to be handled by this bot session
        outgoingDispatcher.addRoute(new PacketEnvelopeFilter<Bot>() {
            @Override
            public boolean accept(PacketEnvelope<Bot> packet) {
                return packet.getLabel().equals(bot);
            }
        }, output);

        // explicit packet output given to bots for sending via output.send
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
