package botto.xmpp;

import botto.xmpp.annotations.PacketOutput;
import botto.xmpp.botto.xmpp.connector.Connector;
import botto.xmpp.botto.xmpp.connector.ConnectorException;
import botto.xmpp.botto.xmpp.connector.ConnectorId;
import botto.xmpp.botto.xmpp.connector.channel.Channel;
import botto.xmpp.botto.xmpp.connector.channel.ChannelContext;
import botto.xmpp.botto.xmpp.connector.channel.ChannelContextListener;
import botto.xmpp.botto.xmpp.connector.channel.ChannelEvent;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.*;
import net.caprazzi.reusables.common.Managed;
import net.caprazzi.reusables.threading.ExecutorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BotManager implements Managed {

    private static final Logger Log = LoggerFactory.getLogger(BotManager.class);
    private final ChannelRegistry channels = new ChannelRegistry();
    private final ConnectorRegistry connectors = new ConnectorRegistry();
    // executes connector.openChannel, connector.closeChannel, connector.send, bot.receive
    private final ListeningExecutorService executor;
    private boolean started;

    private BotManager(ExecutorService executorService) {
        executor = MoreExecutors.listeningDecorator(executorService);
    }

    public static BotManager create() {
        return new BotManager(Executors.newSingleThreadExecutor());
    }

    protected static BotManager create(ExecutorService executorService) {
        return new BotManager(executorService);
    }

    public boolean isStarted() {
        return started;
    }

    @Override
    public void start() {
        if (isStarted()) {
            throw new BottoRuntimeException("Could not start: already started");
        }
        started = true;
        for (Connector connector : connectors.list()) {
            startConnector(connector);
        }
    }

    @Override
    public void stop() {
        if (!isStarted()) {
            throw new BottoRuntimeException("Could not stop: not started");
        }
        started = false;
        try {
            ExecutorUtils.shutdown(Log, executor, 2, TimeUnit.SECONDS);
            for (Connector connector : connectors.list()) {
                stopConnector(connector);
            }
        } catch (Exception ex) {
            Log.error("Error during shutdown - ignoring: {}", ex);
        }
    }

    public void addChannelEventListener(ChannelContextListener listener) {
        channels.addChannelContextListener(listener);
    }

    // TODO: why is this sinchronized and not other methods?
    public synchronized ConnectorId registerConnector(final Connector connector) throws BottoException {
        ConnectorId connectorId = connectors.addConnector(connector);
        connector.addChannelListener(new ConnectorChannelListener(this, connector, connectorId));
        Log.info("Registered connector {} with id {}", connector, connectorId);
        if (isStarted()) {
            startConnector(connector);
        }
        return connectorId;
    }

    public synchronized void removeConnector(ConnectorId connectorId) {
        Log.info("Removing connector {}", connectorId);
        Connector removed = connectors.removeConnector(connectorId);
        stopConnector(removed);
    }

    public ListenableFuture<ChannelContext> addBot(final ConnectorId connectorId, final JID address, final AbstractBot bot) {
        return async(message("opening new channel for {}::{} on {}", address, bot, connectorId), new Callable<ChannelContext>() {
            @Override
            public ChannelContext call() throws Exception {
                final Connector connector = connectors.getConnector(connectorId);
                ChannelContext context = connector.openChannel(address);
                bot.setContext(new ChannelBotContext(context));
                bot.setPacketOutput(new ConnectorPacketOutput(BotManager.this, connector, context.getChannel()));
                channels.addChannel(context, bot);
                return context;
            }
        });
    }

    public ListenableFuture<Void> removeBot(final ConnectorId connectorId, final JID address, final AbstractBot bot) {
        return async(message("Removing bot {}::{} on {}", bot, address, connectorId), new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Connector connector = connectors.getConnector(connectorId);
                Channel channel = channels.getChannel(address);
                connector.closeChannel(channel);
                channels.removeChannel(channel);
                return null;
            }
        });
    }

    // TODO: return a future so any error can easily be reported back
    void send(final Connector connector, final Channel channel, final Packet packet) {
        async(message("Sending packet to {}::{}: {}", channel, connector, packet), new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                connector.send(channel, packet);
                return null;
            }
        });
    }

    void receive(final Connector connector, final Channel channel, final Packet packet, final Meters.ConnectorMetrics meter) {
        Log.debug("Received packet on {}::{}: {}", channel, connector, packet);

        final AbstractBot bot = channels.getBot(channel);
        ListenableFuture<Packet> execute = deliverToBot(packet, bot, meter);
        Futures.addCallback(execute, new FutureCallback<Packet>() {
            public void onSuccess(Packet response) {
                Log.debug("Delivered packet to bot {}: {}, with response {}", bot, packet.getID(), response);
                if (response != null) {
                    meter.countBotResponse();
                    send(connector, channel, response);
                }
            }

            public void onFailure(Throwable t) {
                Log.error("Error while delivering packet {} to {} on {}", packet, channel, connector);
                meter.countDeliveryError();
            }
        });
    }

    public void setChannelEvent(ChannelEvent event) {
        channels.setChannelEvent(event);
    }

    private ListenableFuture<Packet> deliverToBot(final Packet packet, final AbstractBot bot, final Meters.ConnectorMetrics metrics) {
        return executor.submit(new Callable<Packet>() {
            public Packet call() throws Exception {
                long start = metrics.startBotDelivery();
                try {
                    return bot.receive(packet);
                } catch (Exception ex) {
                    throw new BottoRuntimeException(ex, "Failed to deliver packet {0} to bot {1}", packet, bot);
                } finally {
                    metrics.timeBotDelivery(start);
                }
            }
        });
    }

    private void startConnector(Connector connector) {
        try {
            connector.start();
        } catch (ConnectorException e) {
            throw new BottoRuntimeException(e, "Could not start Connector {0}", connector);
        }
    }

    private void stopConnector(Connector connector) {
        try {
            connector.stop();
        } catch (ConnectorException e) {
            throw new BottoRuntimeException(e, "Error while stopping connector {0}", connector);
        }
    }

    private String message(String format, Object... args) {
        return MessageFormatter.arrayFormat(format, args).getMessage();
    }

    private <T> ListenableFuture<T> async(final String message, final Callable<T> callable) {
        Log.debug("Executing: {} ", message);
        return executor.submit(new Callable<T>() {
            @Override
            public T call() throws Exception {
                try {
                    T value = callable.call();
                    Log.debug("Success: {}", message);
                    return value;
                } catch (Throwable t) {
                    Log.error("Failure: {}", message);
                    throw new BottoException(t, "Failure: {0}", message);
                }
            }
        });
    }

    public static class ConnectorPacketOutput implements PacketOutput {

        private final BotManager manager;
        private final Connector connector;
        private final Channel channel;

        public ConnectorPacketOutput(BotManager manager, Connector connector, Channel channel) {

            Preconditions.checkNotNull(manager);
            Preconditions.checkNotNull(connector);
            Preconditions.checkNotNull(channel);

            this.manager = manager;
            this.connector = connector;
            this.channel = channel;
        }

        @Override
        public void send(Packet packet) {
            manager.send(connector, channel, packet);
        }
    }

}
