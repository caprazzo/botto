package botto.xmpp.examples.mock;

import botto.xmpp.AbstractBot;
import botto.xmpp.BotManager;
import botto.xmpp.Meters;
import botto.xmpp.botto.xmpp.connector.ConnectorId;
import botto.xmpp.connectors.mock.MockConnector;
import botto.xmpp.connectors.mock.MockConnectorConfiguration;
import botto.xmpp.examples.bots.EchoBot;
import botto.xmpp.examples.bots.SpamBot;
import botto.xmpp.service.reflection.AnnotatedBotObject;
import ch.qos.logback.classic.Level;
import com.codahale.metrics.JmxReporter;
import org.xmpp.packet.JID;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PerformanceTestingEngine {

    public static void main(String[] args) throws Exception {

        // start metrics reporting to JMX
        final JmxReporter reporter = JmxReporter.forRegistry(Meters.Metrics).build();
        reporter.start();

        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);

        ScheduledExecutorService service = Executors.newScheduledThreadPool(10);

        BotManager connectionManager = BotManager.create();

        MockConnectorConfiguration configuration = new MockConnectorConfiguration("example.com");
        configuration.setDomain("example.com");
        MockConnector connector = new MockConnector(configuration);

        ConnectorId connectorId = connectionManager.registerConnector(connector);


        // 100k bots generating traffic that goes nowhere
        for (int i = 0; i < 1000000; i++) {

            JID echoAddress = new JID("echo" + i + "@example.com");
            JID spamAddress = new JID("spam" + i + "@example.com");

            // setup a bot that keep sending messages to the echo bot
            SpamBot spamBot = new SpamBot(echoAddress);
            AbstractBot spamAnnotatedBot = AnnotatedBotObject.from(spamBot).get();

            connectionManager.addBot(connectorId, spamAddress, spamAnnotatedBot);


            service.scheduleAtFixedRate(spamBot, 10, 1, TimeUnit.SECONDS);

            EchoBot echoBot = new EchoBot();
            AbstractBot echo = AnnotatedBotObject.from(echoBot).get();
            connectionManager.addBot(connectorId, echoAddress, echo);
        }

        connector.start();
        connectionManager.start();
    }

}
