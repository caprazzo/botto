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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BotManagerTest {

    private BotManager botManager;
    private Connector firstConnector = EasyMock.createMock(Connector.class);
    private Connector secondConnector = EasyMock.createMock(Connector.class);
    private TestBot firstBot = new TestBot();
    private TestBot secondBot = new TestBot();
    private ConnectorId firstConnectorId = createMock(ConnectorId.class);
    private Message firstMessage;
    private JID addressFirstBot;
    private JID addressSecondBot;
    private Message secondMessage;
    private ConnectorId secondConnectorId = createMock(ConnectorId.class);

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
        Connector connector = mock(Connector.class);
        ConnectorId id = mock(ConnectorId.class);
        when(connector.getConnectorId()).thenReturn(id);
        botManager.registerConnector(connector);
        verify(connector).addChannelListener(any(ConnectorChannelListener.class));
        verify(connector).start();
    }

    @Test
    public void add_remove_connector() throws ConnectorException, BottoException {
        Connector connector = mock(Connector.class);
        ConnectorId id = mock(ConnectorId.class);
        when(connector.getConnectorId()).thenReturn(id);

        botManager.registerConnector(connector);
        botManager.removeConnector(connector);

        verify(connector).addChannelListener(any(ConnectorChannelListener.class));
        verify(connector).start();
        verify(connector).stop();
    }

    @Test
    public void add_bot() throws BottoException, ConnectorException {
        Connector connector = mock(Connector.class);
        ConnectorId connectorId = mock(ConnectorId.class);
        when(connector.getConnectorId()).thenReturn(connectorId);
        ChannelContext context = mock(ChannelContext.class);
        Channel channel = mock(Channel.class);
        FutureCallback<ChannelContext> addCallback = mock(FutureCallback.class);

        botManager.registerConnector(connector);


        when(connector.openChannel(addressFirstBot)).thenReturn(context);
        when(context.getChannel()).thenReturn(channel);
        when(channel.getAddress()).thenReturn(addressFirstBot);

        ListenableFuture<ChannelContext> future = botManager.addBot(connector.getConnectorId(), addressFirstBot, firstBot);

        Futures.addCallback(future, addCallback);

        verify(connector).openChannel(addressFirstBot);
        verify(addCallback).onSuccess(context);
    }

    @Test
    public void add_remove_bot() throws BottoException, ConnectorException {
        Connector connector = mock(Connector.class);
        ConnectorId id = mock(ConnectorId.class);
        when(connector.getConnectorId()).thenReturn(id);

        ChannelContext context = mock(ChannelContext.class);
        Channel channel = mock(Channel.class);
        FutureCallback<Void> removeCallback = mock(FutureCallback.class);

        when(connector.openChannel(addressFirstBot)).thenReturn(context);
        when(context.getChannel()).thenReturn(channel);
        when(channel.getAddress()).thenReturn(addressFirstBot);

        botManager.registerConnector(connector);

        botManager.addBot(connector.getConnectorId(), addressFirstBot, firstBot);

        ListenableFuture<Void> future = botManager.removeBot(connector.getConnectorId(), addressFirstBot, firstBot);

        Futures.addCallback(future, removeCallback);

        verify(removeCallback).onSuccess(null);
    }

    @Test
    public void send_message() {
        //1. register connector

        //2. add bot

        //3. send message
    }

    @Test
    public void add_bot_with_openchannel_failure() throws BottoException, ConnectorException, ExecutionException, InterruptedException {
        Connector connector = mock(Connector.class);
        ConnectorId id = mock(ConnectorId.class);
        when(connector.getConnectorId()).thenReturn(id);

        botManager.registerConnector(connector);

        FutureCallback<ChannelContext> callback = mock(FutureCallback.class);

        RuntimeException exception = new RuntimeException("Random open channel failure");
        when(connector.openChannel(addressFirstBot)).thenThrow(exception);

        ListenableFuture<ChannelContext> future = botManager.addBot(connector.getConnectorId(), addressFirstBot, firstBot);

        Futures.addCallback(future, callback);
        verify(callback).onFailure(any(Throwable.class));
    }

}