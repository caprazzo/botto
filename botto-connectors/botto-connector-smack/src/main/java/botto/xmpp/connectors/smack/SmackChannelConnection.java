package botto.xmpp.connectors.smack;

import botto.xmpp.botto.xmpp.connector.*;

import botto.xmpp.botto.xmpp.connector.channel.Channel;
import botto.xmpp.botto.xmpp.connector.channel.ChannelConnection;
import botto.xmpp.botto.xmpp.connector.channel.ChannelEvent;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.jivesoftware.smack.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.Packet;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A ChannelConnection that uses the Smack library.
 *
 * Each connection wraps an XMPPConnection, which is allocated in its own thread.
 */
class SmackChannelConnection implements ChannelConnection {

    private final static Logger Log = LoggerFactory.getLogger(SmackChannelConnection.class);

    private final SmackConnector connector;
    private final Channel channel;
    private final String secret;
    private final String resource;

    private final XMPPConnection connection;

    private final ExecutorService connectionExecutor = Executors.newSingleThreadExecutor();

    // TODO: add a SmackBotConfiguration object, or create the XMPPConnection outside
    public SmackChannelConnection(SmackConnector connector, Channel channel, String host, int port, String secret, String resource) {
        this.connector = connector;
        this.channel = channel;

        this.secret = secret;
        this.resource = resource;

        // TODO: smack specific behaviour should be configurable
        ConnectionConfiguration configuration = new ConnectionConfiguration(host, port);
        configuration.setReconnectionAllowed(false);
        configuration.setSendPresence(true);
        configuration.setCompressionEnabled(true);

        Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.accept_all);
        connection = new XMPPConnection(configuration);
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    synchronized void send(Packet packet) {
        if (!connection.isConnected()) {
            if (Log.isDebugEnabled())
                Log.warn("Not sending packet because connection is not connected. Packet: {}", packet.toXML());

            throw new RuntimeException("Not sending packet because connection is not connected. Packet: {}" + packet.toXML());
        }

        if (!connection.isAuthenticated()) {
            if (Log.isDebugEnabled())
                Log.warn("Not sending packet because connection is not authenticated. Packet: {}", packet.toXML());

            throw new RuntimeException("Not sending packet because connection is not authenticated. Packet: {} " + packet.toXML());
        }

        if (Log.isDebugEnabled()) {
            Log.debug("Sending packet {}", packet);
        }

        org.jivesoftware.smack.packet.Packet converted;
        try {
            converted = PacketTypeConverter.convertFromTinder(packet, connection);
        }
        catch(Exception ex) {
            Log.error("Error while converting packet from Tinder to Smack. Packet: {}, Exception: {}", packet, ex);
            throw new RuntimeException("Error while converting packet from Tinder to Smack. Packet: " + packet, ex);
        }

        try {
            connection.sendPacket(converted);
        }
        catch (Exception ex) {
            Log.error("Error while sending packet {}", packet, ex);
            throw new RuntimeException("Error while sending packet " + packet, ex);
        }
    }

    @Override
    public Connector getConnector() {
        return connector;
    }

    public void stop() {
        // TODO: remove conn listener
        // connection.removeConnectionListener();

        // TODO: remove packet listener
        // connection.removePacketListener();

        // disconnect
        if (connection.isConnected()) {
            connector.channelEvent(ChannelEvent.disconnecting(channel, "Connection stop requested"));
            connection.disconnect();
        }
    }

    public synchronized void start() {
        final SmackChannelConnection botConnection = this;
        connector.channelEvent(ChannelEvent.connecting(channel));
        Futures.addCallback(connect(), new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Log.info("Connection Successful");

                //connection.
                connection.addConnectionListener(new SmackConnectionListener());
                connection.addPacketListener(new PacketListener() {
                    @Override
                    public void processPacket(org.jivesoftware.smack.packet.Packet packet) {
                        Log.debug("Received packet {}", packet.toXML());

                        Packet converted;

                        try {
                            converted = PacketTypeConverter.converttoTinder(packet);
                        }
                        catch (Exception ex) {
                            Log.error("Error while converting packet from Smack to Tinder. Packet: {}, Exception: {}", packet, ex);
                            throw new RuntimeException("Error while converting packet from Smack to Tinder. Packet: " + packet, ex);
                        }

                        try {
                            connector.receiveFromConnection(channel, converted);
                        }
                        catch(Exception ex) {
                           Log.error("Error while processing packet {}: {}", packet.toXML(), ex);
                        }
                    }
                }, null);

                connector.channelEvent(ChannelEvent.connected(channel));
            }

            @Override
            public void onFailure(Throwable t) {
                Log.error("Connection failed", t);
            }
        });
    }

    private ListenableFuture<Boolean> connect() {
        final SettableFuture<Boolean> future = SettableFuture.create();
        connectionExecutor.submit(new Runnable() {

            private int count = 0;

            @Override
            public void run() {
                Log.info("Connecting {}... #{}", channel.getAddress(), count++);

                if (connection.isConnected()) {
                    connection.disconnect();
                }
                Log.trace("connection is connected {}", connection.isConnected());
                while (!connection.isConnected()) {
                    try {
                        Log.trace("before connect()");
                        connection.connect();
                        Log.trace("after connect()");
                        count = 0;
                    }
                    catch (XMPPException ex) {
                        int retryMillis = getReconnectDelay(count);
                        Log.error("Could not connect to {}:{}. Retry in " + retryMillis+ "ms. Error: " + ex.getMessage(), connection.getHost(), connection.getPort());
                        sleep(getReconnectDelay(count));
                    }
                }

                try {
                    Log.trace("before login()");
                    connection.login(channel.getAddress().getNode(), secret, resource);
                    Log.trace("after login()");
                    future.set(true);
                }
                catch (XMPPException ex) {
                    connector.channelEvent(ChannelEvent.disconnected(channel, "Login failed", ex));
                    future.setException(new ConnectorException("Could not login as '{}'. Error: {}", channel.getAddress().getNode(), ex.getMessage()));
                }

                Log.trace("exit connect thread");
            }

            private void sleep(long reconnectDelay) {
                try {
                    Thread.sleep(reconnectDelay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            private int getReconnectDelay(int count) {
                return 5000;
            }
        });
        return future;
    }

    private class SmackConnectionListener implements ConnectionListener {

        @Override
        public void connectionClosed() {
            Log.info("Connection {}: closed", connection);
            // smack's embedded connection logic does not seem to work without this fix
            // Could it be related to threading?
            //if (connection.isConnected())
            //    connection.disconnect();
            connector.channelEvent(ChannelEvent.disconnected(channel, "Disconnected"));
            connect();
        }

        @Override
        public void connectionClosedOnError(Exception e) {
            Log.warn("Connection {}: closed with exception {}", connection, e);
            connector.channelEvent(ChannelEvent.disconnected(channel, "Disconnected with error", e));
        }

        @Override
        public void reconnectingIn(int i) {
            Log.info("Connection {}: reconnecting in ", i);
            connector.channelEvent(ChannelEvent.connecting(channel, "reconnecting in " + i + "s"));
        }

        @Override
        public void reconnectionSuccessful() {
            Log.info("Connection for {}: reconnected", channel);
            connector.channelEvent(ChannelEvent.connected(channel, "Reconnection successful"));
        }

        @Override
        public void reconnectionFailed(Exception e) {
            Log.error("Connection {}: reconnection failed with exception {}", e);
            connector.channelEvent(ChannelEvent.disconnected(channel, "Reconnection failed with error ", e));
        }
    }
}
