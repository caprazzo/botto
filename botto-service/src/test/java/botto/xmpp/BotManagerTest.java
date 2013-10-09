package botto.xmpp;

import botto.xmpp.botto.xmpp.connector.Connector;
import botto.xmpp.botto.xmpp.connector.ConnectorException;
import botto.xmpp.botto.xmpp.connector.ConnectorId;
import botto.xmpp.botto.xmpp.connector.channel.Channel;
import botto.xmpp.botto.xmpp.connector.channel.ChannelContext;
import botto.xmpp.service.dispatcher.ListenableConfirmation;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import com.google.common.util.concurrent.MoreExecutors;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

import java.util.concurrent.ExecutionException;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

public class BotManagerTest {

    private BotManager botManager;
    private Connector firstConnector = EasyMock.createMock(Connector.class);
    private Connector secondConnector = EasyMock.createMock(Connector.class);
    private TestBot firstBot = new TestBot();
    private TestBot secondBot = new TestBot();
    private ConnectorId firstConnectorId;
    private Message firstMessage;
    private JID addressFirstBot;
    private JID addressSecondBot;
    private Message secondMessage;
    private ConnectorId secondConnectorId;

    @Before
    public void setUp() throws Exception {
        botManager = BotManager.create(MoreExecutors.sameThreadExecutor());
        botManager.start();

        addressFirstBot = new JID("first_bot@example.com");
        addressSecondBot = new JID("second_bot@example.com");

        JID dest = new JID("dest@example.com");

        firstMessage = new Message();
        firstMessage.setBody("first_message");
        firstMessage.setTo(dest);

        secondMessage = new Message();
        secondMessage.setBody("second_message");
        secondMessage.setTo(dest);
    }

    @After
    public void tearDown() throws Exception {
        try {
            botManager.stop();
        }
        catch(Exception ex) { }
    }

    @Test
    public void add_connector() throws BottoException, ConnectorException {
        Connector connector = Mockito.mock(Connector.class);
        botManager.registerConnector(connector);
        Mockito.verify(connector).addChannelListener(Mockito.any(ConnectorChannelListener.class));
        Mockito.verify(connector).start();
    }

    @Test
    public void add_remove_connector() throws ConnectorException, BottoException {
        Connector connector = Mockito.mock(Connector.class);

        ConnectorId connectorId = botManager.registerConnector(connector);
        botManager.removeConnector(connectorId);

        Mockito.verify(connector).addChannelListener(Mockito.any(ConnectorChannelListener.class));
        Mockito.verify(connector).start();
        Mockito.verify(connector).stop();
    }

    @Test
    public void add_bot() throws BottoException, ConnectorException {
        Connector connector = Mockito.mock(Connector.class);
        ChannelContext context = Mockito.mock(ChannelContext.class);
        Channel channel = Mockito.mock(Channel.class);
        FutureCallback<ChannelContext> addCallback = Mockito.mock(FutureCallback.class);
        ConnectorId connectorId = botManager.registerConnector(connector);

        Mockito.when(connector.openChannel(addressFirstBot)).thenReturn(context);
        Mockito.when(context.getChannel()).thenReturn(channel);
        Mockito.when(channel.getAddress()).thenReturn(addressFirstBot);

        ListenableFuture<ChannelContext> future = botManager.addBot(connectorId, addressFirstBot, firstBot);

        Futures.addCallback(future, addCallback);

        Mockito.verify(connector).openChannel(addressFirstBot);
        Mockito.verify(addCallback).onSuccess(context);
    }

    @Test
    public void add_remove_bot() throws BottoException, ConnectorException {
        Connector connector = Mockito.mock(Connector.class);
        ChannelContext context = Mockito.mock(ChannelContext.class);
        Channel channel = Mockito.mock(Channel.class);
        FutureCallback<Boolean> removeCallback = Mockito.mock(FutureCallback.class);

        Mockito.when(connector.openChannel(addressFirstBot)).thenReturn(context);
        Mockito.when(context.getChannel()).thenReturn(channel);
        Mockito.when(channel.getAddress()).thenReturn(addressFirstBot);

        ConnectorId connectorId = botManager.registerConnector(connector);

        botManager.addBot(connectorId, addressFirstBot, firstBot);

        ListenableConfirmation future = botManager.removeBot(connectorId, addressFirstBot, firstBot);

        Futures.addCallback(future, removeCallback);

        Mockito.verify(removeCallback).onSuccess(true);
    }

    @Test
    public void send_message() {
        //1. register connector

        //2. add bot

        //3. send message
    }

    @Test
    public void add_bot_with_openchannel_failure() throws BottoException, ConnectorException, ExecutionException, InterruptedException {
        Connector connector = Mockito.mock(Connector.class);
        ConnectorId id = botManager.registerConnector(connector);

        FutureCallback<ChannelContext> callback = Mockito.mock(FutureCallback.class);

        RuntimeException exception = new RuntimeException("Random open channel failure");
        Mockito.when(connector.openChannel(addressFirstBot)).thenThrow(exception);

        ListenableFuture<ChannelContext> future = botManager.addBot(id, addressFirstBot, firstBot);

        Futures.addCallback(future, callback);
        Mockito.verify(callback).onFailure(exception);
    }

    //@Test
    public void add_two_bots() throws ConnectorException, BottoException, ExecutionException, InterruptedException {

        Channel firstChannel = Channel.from(addressFirstBot);
        ChannelContext firstContext = ChannelContext.of(firstChannel);
        expect(firstConnector.openChannel(addressFirstBot)).andReturn(firstContext);

        Channel secondChannel = Channel.from(addressSecondBot);
        ChannelContext secondContext = ChannelContext.of(secondChannel);
        expect(firstConnector.openChannel(addressSecondBot)).andReturn(secondContext);

        firstConnector.addChannelListener(anyObject(ConnectorChannelListener.class));

        firstConnector.send(firstChannel, firstMessage);
        firstConnector.send(secondChannel, secondMessage);

        firstConnector.doStart();
        firstConnector.doStop();

        EasyMock.replay(firstConnector);

        firstConnectorId = botManager.registerConnector(firstConnector);

        //botManager.start();

        ChannelContext createdFirstContext = botManager.addBot(firstConnectorId, addressFirstBot, firstBot).get();
        assertEquals(createdFirstContext, firstContext);
        assertEquals(addressFirstBot, createdFirstContext.getChannel().getAddress());

        ChannelContext createdSecondContext = botManager.addBot(firstConnectorId, addressSecondBot, secondBot).get();
        assertEquals(createdSecondContext, secondContext);
        assertEquals(addressSecondBot, createdSecondContext.getChannel().getAddress());

        firstBot.send(firstMessage);
        secondBot.send(secondMessage);

        //botManager.stop();

        verify(firstConnector);
    }

    //@Test
    public void add_two_bots_two_connectors() throws ConnectorException, BottoException, ExecutionException, InterruptedException {

        Channel firstChannel = Channel.from(addressFirstBot);
        ChannelContext firstContext = ChannelContext.of(firstChannel);
        expect(firstConnector.openChannel(addressFirstBot)).andReturn(firstContext);

        Channel secondChannel = Channel.from(addressSecondBot);
        ChannelContext secondContext = ChannelContext.of(secondChannel);
        expect(secondConnector.openChannel(addressSecondBot)).andReturn(secondContext);

        firstConnector.addChannelListener(anyObject(ConnectorChannelListener.class));
        secondConnector.addChannelListener(anyObject(ConnectorChannelListener.class));

        firstConnector.send(firstChannel, firstMessage);
        secondConnector.send(secondChannel, secondMessage);

        firstConnector.doStart();
        firstConnector.doStop();

        secondConnector.doStart();
        secondConnector.doStop();

        EasyMock.replay(firstConnector);
        EasyMock.replay(secondConnector);

        firstConnectorId = botManager.registerConnector(firstConnector);
        secondConnectorId = botManager.registerConnector(secondConnector);

        //botManager.start();

        ChannelContext createdFirstContext = botManager.addBot(firstConnectorId, addressFirstBot, firstBot).get();
        assertEquals(createdFirstContext, firstContext);
        assertEquals(addressFirstBot, createdFirstContext.getChannel().getAddress());

        ChannelContext createdSecondContext = botManager.addBot(secondConnectorId, addressSecondBot, secondBot).get();
        assertEquals(createdSecondContext, secondContext);
        assertEquals(addressSecondBot, createdSecondContext.getChannel().getAddress());

        firstBot.send(firstMessage);
        secondBot.send(secondMessage);

        //botManager.stop();

        verify(firstConnector);
    }

    @Test
    public void test_connector_packet_output() {
        BotManager manager = Mockito.mock(BotManager.class);
        Connector connector = Mockito.mock(Connector.class);
        Channel channel = Mockito.mock(Channel.class);
        Packet packet = Mockito.mock(Packet.class);
        BotManager.ConnectorPacketOutput packetOutput = new BotManager.ConnectorPacketOutput(manager, connector, channel);

        packetOutput.send(packet);

        Mockito.verify(manager).send(connector, channel, packet);
    }

}