package botto.xmpp.engine;

import botto.xmpp.BottoConnectionManager;
import botto.xmpp.annotations.Context;
import botto.xmpp.annotations.Receive;
import botto.xmpp.botto.xmpp.connector.ConnectorException;
import botto.xmpp.botto.xmpp.connector.ConnectorId;
import botto.xmpp.connectors.smack.SmackConnector;
import botto.xmpp.connectors.smack.SmackConnectorConfiguration;
import botto.xmpp.connectors.whack.WhackConnector;
import botto.xmpp.connectors.whack.WhackConnectorConfiguration;
import botto.xmpp.AbstractBot;
import botto.xmpp.Meters;
import botto.xmpp.service.reflection.AnnotatedBotObject;
import com.codahale.metrics.JmxReporter;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;

public class ExampleEngineMain {

    public static void main(String[] args) throws Exception {

        BottoConnectionManager connectionManager = new BottoConnectionManager();

        // setup whack connector
        WhackConnectorConfiguration whackConfiguration = new WhackConnectorConfiguration();
        whackConfiguration.setDomain("caprazzi.net");
        whackConfiguration.setHost("localhost");
        whackConfiguration.setPort(5275);
        whackConfiguration.setSecret("secret");

        WhackConnector whackConnector = new WhackConnector(whackConfiguration);
        ConnectorId whackConnectorId = connectionManager.registerConnector(whackConnector);

        // setup smack connector
        SmackConnectorConfiguration smackConfiguration = new SmackConnectorConfiguration();
        smackConfiguration.setHost("localhost");
        smackConfiguration.setPort(5222);
        smackConfiguration.setSecret("bot");
        smackConfiguration.setResource("any");

        SmackConnector smackConnector = new SmackConnector(smackConfiguration);
        ConnectorId smackConnectorId = connectionManager.registerConnector(smackConnector);

        try {
            AbstractBot bot = makeBot(new ExampleBot());
            connectionManager.addBot(bot, new JID("bot@caprazzi.net"), smackConnectorId);
        } catch (ConnectorException e) {
            throw new RuntimeException(e);
        }

        try {
            AbstractBot bot = makeBot(new ExampleBot());
            connectionManager.addBot(bot, new JID("bot1@caprazzi.net"), smackConnectorId);
        } catch (ConnectorException e) {
            throw new RuntimeException(e);
        }

        try {
            AbstractBot bot = makeBot(new ExampleBot());
            connectionManager.addBot(bot, new JID("something@bots.caprazzi.net"), whackConnectorId);
        } catch (ConnectorException e) {
            throw new RuntimeException(e);
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

        @Context
        botto.xmpp.annotations.PacketOutput out;

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
