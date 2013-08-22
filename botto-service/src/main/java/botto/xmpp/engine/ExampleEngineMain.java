package botto.xmpp.engine;

import botto.xmpp.annotations.Receive;
import botto.xmpp.connectors.smack.SmackConnector;
import botto.xmpp.connectors.smack.SmackConnectorConfiguration;
import botto.xmpp.connectors.whack.WhackConnector;
import botto.xmpp.connectors.whack.WhackConnectorConfiguration;
import botto.xmpp.service.AbstractBot;
import botto.xmpp.service.reflection.AnnotatedBotObject;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;

public class ExampleEngineMain {

    public static void main(String[] args) {

        ConnectionManager connectionManager = new ConnectionManager();

        WhackConnectorConfiguration whackConfiguration = new WhackConnectorConfiguration();
        whackConfiguration.setDomain("caprazzi.net");
        whackConfiguration.setHost("localhost");
        whackConfiguration.setPort(5275);
        whackConfiguration.setSecret("secret");

        WhackConnector whackConnector = new WhackConnector(whackConfiguration);

        SmackConnectorConfiguration smackConfiguration = new SmackConnectorConfiguration();
        smackConfiguration.setHost("localhost");
        smackConfiguration.setPort(5222);
        smackConfiguration.setSecret("bot");
        smackConfiguration.setResource("any");

        SmackConnector smackConnector = new SmackConnector(smackConfiguration);

        try {
            AbstractBot bot = makeBot(new ABot());
            connectionManager.addBot(bot, new JID("bot@caprazzi.net"), smackConnector);
        } catch (ConnectorException e) {
            throw new RuntimeException(e);
        }

        try {
            AbstractBot bot = makeBot(new ABot());
            connectionManager.addBot(bot, new JID("bot1@caprazzi.net"), smackConnector);
        } catch (ConnectorException e) {
            throw new RuntimeException(e);
        }

        try {
            AbstractBot bot = makeBot(new ABot());
            connectionManager.addBot(bot, new JID("something@bots.caprazzi.net"), whackConnector);
        } catch (ConnectorException e) {
            throw new RuntimeException(e);
        }


        whackConnector.start();
        smackConnector.start();
        connectionManager.start();
    }

    private static AbstractBot makeBot(Object o) {
        return AnnotatedBotObject.from(o).get();
    }

    public static class ABot {
        @Receive
        public void receive(Message message) {
            System.out.println(message);
        }
    }
}
