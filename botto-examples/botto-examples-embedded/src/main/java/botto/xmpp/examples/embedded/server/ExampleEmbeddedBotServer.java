package botto.xmpp.examples.embedded.server;

import botto.xmpp.AbstractBot;
import botto.xmpp.BotManager;
import botto.xmpp.BottoException;
import botto.xmpp.Meters;
import botto.xmpp.botto.xmpp.connector.ConnectorId;
import botto.xmpp.connectors.mock.MockConnector;
import botto.xmpp.connectors.mock.MockConnectorConfiguration;
import botto.xmpp.connectors.smack.SmackConnector;
import botto.xmpp.connectors.smack.SmackConnectorConfiguration;

import botto.xmpp.examples.bots.EchoBot;
import botto.xmpp.examples.bots.SpamBot;
import botto.xmpp.service.reflection.AnnotatedBotObject;
import ch.qos.logback.classic.Level;
import com.codahale.metrics.JmxReporter;
import org.xmpp.packet.JID;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class ExampleEmbeddedBotServer {

    public static void main(String[] args) throws BottoException {
        final JmxReporter reporter = com.codahale.metrics.JmxReporter.forRegistry(Meters.Metrics).build();
        reporter.start();

        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);

        ScheduledExecutorService service = Executors.newScheduledThreadPool(10);

        BotManager botManager = BotManager.create();

        MockConnectorConfiguration configuration = new MockConnectorConfiguration("example.com");
        configuration.setDomain("example.com");
        MockConnector connector = new MockConnector(configuration);

        SmackConnectorConfiguration smackConfiguration = new SmackConnectorConfiguration();
        smackConfiguration.setHost("localhost");
        smackConfiguration.setPort(5222);
        smackConfiguration.setSecret("secret");

        SmackConnector smackConnector = new SmackConnector(smackConfiguration);

        ConnectorId connectorId = botManager.registerConnector(smackConnector);

        JID echoAddress = new JID("echo@caprazzi.net");
        JID spamAddress = new JID("spam@caprazzi.net");

        SpamBot spamBot = new SpamBot(echoAddress);
        AbstractBot spamAnnotatedBot = AnnotatedBotObject.from(spamBot).get();

        botManager.addBot(connectorId, spamAddress, spamAnnotatedBot);

        EchoBot echoBot = new EchoBot();
        AbstractBot echo = AnnotatedBotObject.from(echoBot).get();
        botManager.addBot(connectorId, echoAddress, echo);

        botManager.start();

        //service.scheduleAtFixedRate(spamBot, 1, 5, TimeUnit.SECONDS);
    }
}
