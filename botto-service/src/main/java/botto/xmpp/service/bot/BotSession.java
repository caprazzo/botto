package botto.xmpp.service.bot;


import botto.xmpp.service.AbstractBot;
import botto.xmpp.service.BotConnectionInfo;
import botto.xmpp.utils.PacketTypeConverter;

import net.caprazzi.reusables.common.Managed;
import net.caprazzi.reusables.threading.ExecutorUtils;
import org.jivesoftware.smack.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class BotSession implements Managed {

    private final Logger log;

    private final String node;
    private final String secret;
    private final String resource;
    private final XMPPConnection connection;
    private final BotConnectionInfo info;

    private final ExecutorService connectionExecutor = Executors.newSingleThreadExecutor();

    //TODO: wrap configuration in a Configuration object
    public BotSession(String host, int port, String node, String secret, String resource, final AbstractBot bot, final BotSessionPacketSender sender) {
        log = LoggerFactory.getLogger(BotSession.class.getName() + "." + node);

        this.node = node;
        this.secret = secret;
        this.resource = resource;

        ConnectionConfiguration configuration = new ConnectionConfiguration(host, port);
        configuration.setReconnectionAllowed(true);
        configuration.setSendPresence(true);
        configuration.setCompressionEnabled(true);

        Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.accept_all);

        connection = new XMPPConnection(configuration);

        BotSessionPacketOutput packetOutput = new BotSessionPacketOutput(sender, connection, this);

        // deliver incoming packets to the bot and send out the response
        connection.addPacketListener(new PacketListener() {
            @Override
            public void processPacket(org.jivesoftware.smack.packet.Packet packet) {
                try {
                    log.debug("Received packet {}", packet.toXML());

                    //TODO: give any response to packetOutput
                    bot.receive(PacketTypeConverter.converttoTinder(packet));
                }
                catch (Exception ex) {
                    log.error("Error while processing packet {}: {}", packet.getPacketID(), ex);
                }
            }
        }, null);

        // set the packet output to the bot
        bot.setPacketOutput(packetOutput);

        info = new BotConnectionInfo();
        bot.setConnectionInfo(info);
    }

    public synchronized void start() {
        final CountDownLatch latch = new CountDownLatch(1);
        connectionExecutor.submit(new Runnable() {
            @Override
            public void run() {
                log.info("Connecting...");

                try {
                    connection.connect();
                    try {
                        connection.login(node, secret, resource);
                    }
                    catch (XMPPException ex) {
                        //stop();
                        log.error("Could not login as '{}'. Error: {}", node, ex.getMessage());
                    }
                } catch (XMPPException ex) {
                    //stop();
                    log.error("Could not connect to {}:{}. Error: {}", connection.getHost(), connection.getPort(), ex.getMessage());
                }
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!connection.isConnected() || !connection.isAuthenticated()) {
            log.error("Connection Failed");
            stop();
            return;
        }

        connection.addConnectionListener(new ConnectionListener() {
            @Override
            public void connectionClosed() {
                log.info("Connection {}: closed", connection);
                info.setConnectionStatus(false);
            }

            @Override
            public void connectionClosedOnError(Exception e) {
                log.warn("Connection {}: closed with exception {}", connection, e);
                info.setConnectionStatus(false);
            }

            @Override
            public void reconnectingIn(int i) {
                log.info("Connection {}: reconnecting in ", i);
                info.setConnectionStatus(false);
            }

            @Override
            public void reconnectionSuccessful() {
                log.info("Connection {}: reconnected");
                info.setConnectionStatus(true);
            }

            @Override
            public void reconnectionFailed(Exception e) {
                log.error("Connection {}: reconnection failed with exception {}", e);
                info.setConnectionStatus(false);
            }
        });

        log.info("Connected...");
    }

    public synchronized void stop() {
        connection.disconnect();
        ExecutorUtils.shutdown(log, connectionExecutor, 5, TimeUnit.SECONDS);
    }

    public synchronized boolean sendPacket(org.jivesoftware.smack.packet.Packet packet) {

        if (!connection.isConnected()) {
            if (log.isDebugEnabled())
                log.debug("Not sending packet because connection is not connected. Packet: {}", packet.toXML());

            return false;
        }

        if (!connection.isAuthenticated()) {
            if (log.isDebugEnabled())
                log.debug("Not sending packet because connection is not authenticated. Packet: {}", packet.toXML());

            return false;
        }

        if (log.isDebugEnabled()) {
            log.debug("Sending packet {}", packet);
        }
        connection.sendPacket(packet);

        return true;
    }

    @Override
    public String toString() {
        return "BotSession(" + node + ")";
    }
}
