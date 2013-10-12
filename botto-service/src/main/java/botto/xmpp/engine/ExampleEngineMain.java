package botto.xmpp.engine;

import botto.xmpp.BotManager;
import botto.xmpp.annotations.Context;
import botto.xmpp.annotations.Receive;
import botto.xmpp.botto.xmpp.connector.ConnectorId;
import botto.xmpp.botto.xmpp.connector.channel.ChannelContext;
import botto.xmpp.connectors.smack.SmackConnector;
import botto.xmpp.connectors.smack.SmackConnectorConfiguration;
import botto.xmpp.connectors.whack.WhackConnector;
import botto.xmpp.connectors.whack.WhackConnectorConfiguration;
import botto.xmpp.AbstractBot;
import botto.xmpp.Meters;
import botto.xmpp.reflection.AnnotatedBotObject;
import com.codahale.metrics.JmxReporter;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;

import java.util.concurrent.atomic.AtomicInteger;

public class ExampleEngineMain {

    private final static AtomicInteger connectorCount = new AtomicInteger();

    public static void main(String[] args) throws Exception {

        BotManager connectionManager = BotManager.create();

        // setup whack connector
        WhackConnectorConfiguration whackConfiguration = new WhackConnectorConfiguration();
        whackConfiguration.setDomain("caprazzi.net");
        whackConfiguration.setHost("localhost");
        whackConfiguration.setPort(5275);
        whackConfiguration.setSecret("secret");


        ConnectorId whackConnectorId = new ConnectorId(connectorCount.getAndIncrement(), WhackConnector.class, "localhost/caprazzi.net");
        WhackConnector whackConnector = new WhackConnector(whackConnectorId, whackConfiguration);
        connectionManager.registerConnector(whackConnector);

        // setup smack connector
        SmackConnectorConfiguration smackConfiguration = new SmackConnectorConfiguration();
        smackConfiguration.setHost("localhost");
        smackConfiguration.setPort(5222);
        smackConfiguration.setSecret("bot");
        smackConfiguration.setResource("any");

        ConnectorId smackConnectorId = new ConnectorId(connectorCount.getAndIncrement(), SmackConnector.class, "localhost/caprazzi.net");
        SmackConnector smackConnector = new SmackConnector(smackConnectorId, smackConfiguration);
        connectionManager.registerConnector(smackConnector);


        {
            AbstractBot bot = makeBot(new ExampleBot());
            Futures.addCallback(connectionManager.addBot(smackConnectorId, new JID("bot@caprazzi.net"), bot), new FutureCallback<ChannelContext>() {
                @Override
                public void onSuccess(ChannelContext result) {
                    // bot added
                }

                @Override
                public void onFailure(Throwable t) {
                    // failed to add bot
                }
            });
        }

        {
            AbstractBot bot = makeBot(new ExampleBot());
            connectionManager.addBot(smackConnectorId, new JID("bot1@caprazzi.net"), bot);
        }

        {
            AbstractBot bot = makeBot(new ExampleBot());
            connectionManager.addBot(whackConnectorId, new JID("something@bots.caprazzi.net"), bot);
        }

        whackConnector.start();
        smackConnector.start();
        connectionManager.start();

        // start metrics reporting to JMX
        final JmxReporter reporter = JmxReporter.forRegistry(Meters.Metrics).build();
        reporter.start();
    }

    private static AbstractBot makeBot(Object o) {
        return AnnotatedBotObject.from(o).get();
    }

    public static class ExampleBot {

        @Receive
        public Message receive(Message message) {
            System.out.println("Received message " + message.getID());
            Message response = new Message();
            response.setBody("you fool just said: " + message.getBody());


            response.setTo(message.getFrom());
            //out.send(response);
            return response;
        }

        @Override
        public String toString() {
            return "ExampleBot";
        }
    }
}
