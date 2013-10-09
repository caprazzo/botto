package botto.xmpp;

import botto.xmpp.annotations.BotContext;
import botto.xmpp.annotations.PacketOutput;
import botto.xmpp.botto.xmpp.connector.Connector;
import botto.xmpp.botto.xmpp.connector.ConnectorException;
import botto.xmpp.botto.xmpp.connector.ConnectorId;
import botto.xmpp.botto.xmpp.connector.channel.Channel;
import botto.xmpp.botto.xmpp.connector.channel.ChannelContext;
import com.google.common.util.concurrent.ListenableFuture;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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
        botManager = BotManager.create();

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

    }

    @Test
    public void add_bot() throws ExecutionException, InterruptedException, ConnectorException, BottoException {

        Channel channel = Channel.from(addressFirstBot);
        ChannelContext context = ChannelContext.of(channel);
        expect(firstConnector.openChannel(addressFirstBot)).andReturn(context);
        firstConnector.addChannelListener(anyObject(ConnectorChannelListener.class));
        firstConnector.send(channel, firstMessage);
        firstConnector.doStart();
        firstConnector.doStop();

        EasyMock.replay(firstConnector);

        firstConnectorId = botManager.registerConnector(firstConnector);

        botManager.start();

        ListenableFuture<ChannelContext> added = botManager.addBot(firstConnectorId, addressFirstBot, firstBot);
        ChannelContext createdContext = added.get();
        assertEquals(createdContext, context);
        assertEquals(addressFirstBot, context.getChannel().getAddress());

        firstBot.send(firstMessage);

        botManager.stop();

        verify(firstConnector);
    }

    @Test
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

        botManager.start();

        ChannelContext createdFirstContext = botManager.addBot(firstConnectorId, addressFirstBot, firstBot).get();
        assertEquals(createdFirstContext, firstContext);
        assertEquals(addressFirstBot, createdFirstContext.getChannel().getAddress());

        ChannelContext createdSecondContext = botManager.addBot(firstConnectorId, addressSecondBot, secondBot).get();
        assertEquals(createdSecondContext, secondContext);
        assertEquals(addressSecondBot, createdSecondContext.getChannel().getAddress());

        firstBot.send(firstMessage);
        secondBot.send(secondMessage);

        botManager.stop();

        verify(firstConnector);
    }

    @Test
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

        botManager.start();

        ChannelContext createdFirstContext = botManager.addBot(firstConnectorId, addressFirstBot, firstBot).get();
        assertEquals(createdFirstContext, firstContext);
        assertEquals(addressFirstBot, createdFirstContext.getChannel().getAddress());

        ChannelContext createdSecondContext = botManager.addBot(secondConnectorId, addressSecondBot, secondBot).get();
        assertEquals(createdSecondContext, secondContext);
        assertEquals(addressSecondBot, createdSecondContext.getChannel().getAddress());

        firstBot.send(firstMessage);
        secondBot.send(secondMessage);

        botManager.stop();

        verify(firstConnector);
    }


    private static class TestBot extends AbstractBot {

        private PacketOutput getOutput() {
            return output;
        }

        private PacketOutput output;

        @Override
        protected void doSetPacketOutput(PacketOutput output) {
            this.output = output;
        }

        @Override
        protected Packet doReceive(Packet packet) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        protected void doSetcontext(BotContext botContext) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void send(Packet packet) {
            this.output.send(packet);
        }
    }

}
