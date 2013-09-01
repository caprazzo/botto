package botto.xmpp.connectors.smack;

import botto.xmpp.engine.BotConnection;
import botto.xmpp.engine.ConnectionInfoListener;
import botto.xmpp.engine.BotConnectionInfo;
import botto.xmpp.utils.PacketTypeConverter;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.jivesoftware.smack.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A BotConnection that uses the Smack library.
 *
 * Each connection wraps an XMPPConnection, which is allocated in its own thread.
 */
class SmackBotConnection implements BotConnection {

    private final static Logger Log = LoggerFactory.getLogger(SmackBotConnection.class);

    private final JID address;
    private final String secret;
    private final String resource;

    private final XMPPConnection connection;

    private final ExecutorService connectionExecutor = Executors.newSingleThreadExecutor();
    private final BotConnectionInfo connectionInfo = new BotConnectionInfo();
    private ConnectionPacketListener packetListener;
    private ConnectionInfoListener connectionInfoListener;

    public SmackBotConnection(JID address, String host, int port, String secret, String resource) {
        this.address = address;

        this.secret = secret;
        this.resource = resource;

        // TODO: smack specific behaviour should be configurable
        ConnectionConfiguration configuration = new ConnectionConfiguration(host, port);
        configuration.setReconnectionAllowed(true);
        configuration.setSendPresence(true);
        configuration.setCompressionEnabled(true);

        Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.accept_all);
        connection = new XMPPConnection(configuration);
    }

    @Override
    public BotConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    @Override
    public void setConnectionInfoListener(ConnectionInfoListener infoListener) {
        this.connectionInfoListener = infoListener;
    }

    @Override
    public void setConnectionPacketListener(ConnectionPacketListener listener) {
        this.packetListener = listener;
    }

    @Override
    public JID getSendAddress() {
        return address;
    }

    @Override
    public synchronized void send(Packet packet) {
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

    public synchronized void start() {
        Futures.addCallback(connect(), new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Log.info("Connection Succesful");
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
                            // TODO: instead of packetListener, this should call
                            // connector.receive(connection, packet)
                            packetListener.onPacket(converted);
                        }
                        catch(Exception ex) {
                           Log.error("Error while processing packet {}: {}", packet.getPacketID(), ex);
                        }
                    }
                }, null);

                setConnectionStatus(true);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.error("Connection failed", t);
            }
        });
    }

    public void stop() {
        setConnectionStatus(false);

        // TODO: remove conn listener
        // connection.removeConnectionListener();

        // TODO: remove packet listener
        // connection.removePacketListener();

        // disconnect
        if (connection.isConnected()) {
            connection.disconnect();
        }
    }

    private ListenableFuture<Boolean> connect() {
        final SettableFuture<Boolean> future = SettableFuture.create();
        connectionExecutor.submit(new Runnable() {

            // TODO: add retry in case of retriable failure
            @Override
            public void run() {
                Log.info("Connecting...");
                try {
                    connection.connect();
                    try {
                        connection.login(address.getNode(), secret, resource);
                        future.set(true);
                    }
                    catch (XMPPException ex) {
                        Log.error("Could not login as '{}'. Error: {}", address.getNode(), ex.getMessage());
                        future.setException(ex);
                    }
                } catch (XMPPException ex) {
                    Log.error("Could not connect to {}:{}. Error: {}", connection.getHost(), connection.getPort(), ex.getMessage());
                    future.setException(ex);
                }
            }
        });
        return future;
    }

    private void setConnectionStatus(boolean connected) {
        connectionInfo.setConnectionStatus(false);
        if (connectionInfoListener != null)
            connectionInfoListener.onConnectionInfo(connectionInfo);
    }

    private class SmackConnectionListener implements ConnectionListener {

        @Override
        public void connectionClosed() {
            Log.info("Connection {}: closed", connection);
            setConnectionStatus(false);
        }

        @Override
        public void connectionClosedOnError(Exception e) {
            Log.warn("Connection {}: closed with exception {}", connection, e);
            setConnectionStatus(false);
        }

        @Override
        public void reconnectingIn(int i) {
            Log.info("Connection {}: reconnecting in ", i);
            setConnectionStatus(false);
        }

        @Override
        public void reconnectionSuccessful() {
            Log.info("Connection {}: reconnected");
            setConnectionStatus(true);
        }

        @Override
        public void reconnectionFailed(Exception e) {
            Log.error("Connection {}: reconnection failed with exception {}", e);
            setConnectionStatus(false);
        }
    }
}
