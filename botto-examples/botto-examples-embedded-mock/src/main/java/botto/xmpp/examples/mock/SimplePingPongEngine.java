package botto.xmpp.examples.mock;

import botto.xmpp.AbstractBot;
import botto.xmpp.BotManager;
import botto.xmpp.Meters;
import botto.xmpp.botto.xmpp.connector.ConnectorId;
import botto.xmpp.connectors.mock.MockConnector;
import botto.xmpp.connectors.mock.MockConnectorConfiguration;
import botto.xmpp.examples.bots.EchoBot;
import botto.xmpp.examples.bots.SpamBot;
import botto.xmpp.reflection.AnnotatedBotObject;
import ch.qos.logback.classic.Level;
import com.codahale.metrics.JmxReporter;
import org.xmpp.packet.JID;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SimplePingPongEngine {

    public static void main(String[] args) throws Exception {

        // start metrics reporting to JMX
        final JmxReporter reporter = JmxReporter.forRegistry(Meters.Metrics).build();
        reporter.start();

        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.DEBUG);

        ScheduledExecutorService service = Executors.newScheduledThreadPool(10);

        BotManager botManager = BotManager.create();

        MockConnectorConfiguration configuration = new MockConnectorConfiguration("example.com");
        configuration.setDomain("example.com");
        MockConnector connector = new MockConnector(configuration);

        ConnectorId connectorId = botManager.registerConnector(connector);

        JID echoAddress = new JID("echo@example.com");
        JID spamAddress = new JID("spam@example.com");

        SpamBot spamBot = new SpamBot(echoAddress);
        AbstractBot spamAnnotatedBot = AnnotatedBotObject.from(spamBot).get();

        botManager.addBot(connectorId, spamAddress, spamAnnotatedBot);

        EchoBot echoBot = new EchoBot();
        AbstractBot echo = AnnotatedBotObject.from(echoBot).get();
        botManager.addBot(connectorId, echoAddress, echo);

        botManager.start();

        service.scheduleAtFixedRate(spamBot, 1, 5, TimeUnit.SECONDS);
    }
}
