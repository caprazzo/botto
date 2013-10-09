package botto.xmpp;

import botto.xmpp.annotations.PacketOutput;
import botto.xmpp.botto.xmpp.connector.*;
import botto.xmpp.botto.xmpp.connector.channel.Channel;
import botto.xmpp.botto.xmpp.connector.channel.ChannelContext;
import botto.xmpp.botto.xmpp.connector.channel.ChannelContextListener;
import botto.xmpp.botto.xmpp.connector.channel.ChannelEvent;
import botto.xmpp.service.dispatcher.ListenableConfirmation;
import botto.xmpp.utils.Packets;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.*;
import net.caprazzi.reusables.common.Managed;
import net.caprazzi.reusables.threading.ExecutorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        for(Connector connector : connectors.list()) {
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
            for(Connector connector : connectors.list()) {
                stopConnector(connector);
            }
        }
        catch(Exception ex) {
            Log.error("Error during shutdown - ignoring: {}", ex);
        }
    }

    public void addChannelEventListener(ChannelContextListener listener) {
        channels.addChannelContextListener(listener);

    }

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
        Log.info("Adding bot to {}::{}: {}", address, connectorId, bot);
        // TODO: abstractBot should be pre-initialized with a BotContext
        final ChannelBotContext botcontext = new ChannelBotContext(null);
        bot.setContext(botcontext);
        final SettableFuture<ChannelContext> result = SettableFuture.create();
        try {
            final Connector connector = connectors.getConnector(connectorId);

            // asynchronously open channel
            Log.debug("Opening channel for {}|{}", address, connector);
            ListenableFuture<ChannelContext> openChannel = openChannel(connector, address);
            Futures.addCallback(openChannel, new FutureCallback<ChannelContext>() {
                @Override
                public void onSuccess(final ChannelContext context) {
                    Log.debug("Channel {} opened for {}|{}", context, address, connector);
                    botcontext.setChannelContext(context);
                    channels.addChannel(context, bot);
                    bot.setPacketOutput(new PacketOutput() {
                        @Override
                        public void send(Packet packet) {
                            BotManager.this.send(connector, context.getChannel(), packet);
                        }
                    });
                    result.set(context);
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.error("Failed to open channel for {}::{}: {}", address, connector, t);
                    result.setException(new BottoException(t, "Failed to open channel {0}, {1}, {2}", connectorId, address, bot));
                }
            });
        }
        catch(Exception ex) {
            result.setException(ex);
        }
        return result;
    }

    public ListenableConfirmation removeBot(final ConnectorId connectorId, final JID address, final AbstractBot bot) {

        final ListenableConfirmation confirmation = new ListenableConfirmation();
        try {
            final Connector connector = connectors.getConnector(connectorId);
            final Channel channel = channels.getChannel(address);

            // asynchronously close channel
            ListenableFuture<Channel> closeChannel = closeChannel(connector, channel);
            Futures.addCallback(closeChannel, new FutureCallback<Channel>() {
                public void onSuccess(Channel channel) {
                    // TODO: should remove the channel from the registry even if closing fails?
                    channels.removeChannel(channel);
                    confirmation.setSuccess();
                }

                public void onFailure(Throwable t) {
                    confirmation.setFailure(new ConnectorException(t, "Failed to close channel {0}, {1}, {2}", connectorId, address, bot));
                }
            });
        }
        catch(Exception ex) {
            confirmation.setFailure(ex);
        }
        return confirmation;
    }

    private void send(final Connector connector, final Channel channel, final Packet packet) {
        Log.debug("Sending packet to {}::{}: {}", channel, connector, packet);
        ListenableFuture<?> send = executor.submit(new Runnable() {
            public void run() {
                try {
                    // TODO: mind that not all connectors are thread safe
                    connector.send(channel, packet); //Packets.preparePacketForSending(channel, packet));
                } catch (ConnectorException e) {
                    throw new BottoRuntimeException(e, "Exception while submitting to channel {0}. packet {1}", channel, packet);
                }
            }
        });

        Futures.addCallback(send, new FutureCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                Log.debug("Packet sent OK to {}::{}: {}", channel, connector, packet.getID());
            }

            @Override
            public void onFailure(Throwable t) {
                Log.debug("Error sending packet to {}::{}: {}: {}", channel, connector, packet.getID(), t);
                // TODO: count failure
                // TODO: send error to bot
                // channels.getBot(channel).receiveError(packet, t);
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

    private ListenableFuture<ChannelContext> openChannel(final Connector connector, final JID address) {
        return executor.submit(new Callable<ChannelContext>() {
            @Override
            public ChannelContext call() throws Exception {
                ChannelContext channel = connector.openChannel(address);
                Preconditions.checkNotNull(channel);
                return channel;
            }
        });
    }

    private ListenableFuture<Channel> closeChannel(final Connector connector, final Channel channel) {
        return executor.submit(new Callable<Channel>() {
            public Channel call() throws Exception {
                connector.closeChannel(channel);
                return channel;
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

    public void setChannelEvent(ChannelEvent event) {
        channels.setChannelEvent(event);
    }
}
