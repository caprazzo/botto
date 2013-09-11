package botto.xmpp.examples.mock;

import botto.xmpp.BotManager;

import botto.xmpp.botto.xmpp.connector.ConnectorId;
import botto.xmpp.connectors.mock.MockConnector;
import botto.xmpp.connectors.mock.MockConnectorConfiguration;
import botto.xmpp.AbstractBot;
import botto.xmpp.Meters;

import botto.xmpp.examples.bots.EchoBot;
import botto.xmpp.reflection.AnnotatedBotObject;
import ch.qos.logback.classic.Level;
import com.codahale.metrics.JmxReporter;
import org.xmpp.packet.JID;

public class MockEngineMain {

    public static void main(String[] args) throws Exception {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);

        BotManager connectionManager = BotManager.create();

        MockConnectorConfiguration configuration = new MockConnectorConfiguration("example.com");
        configuration.setDomain("example.com");
        MockConnector connector = new MockConnector(configuration);

        ConnectorId mock = connectionManager.registerConnector(connector);

        // echo bot
        EchoBot echoBot = new EchoBot();
        AbstractBot echo = AnnotatedBotObject.from(echoBot).get();
        connectionManager.addBot(mock, new JID("echo@example.com"), echo);

        /*
        // add three generator bots
        {
            SpamBot genBot = new SpamBot(1, TimeUnit.MILLISECONDS, new JID("echo@example.com"));
            AbstractBot gen = AnnotatedBotObject.from(genBot).get();
            connectionManager.addBot(gen, new JID("gen@example.com"), mock);
        }

        {
            SpamBot genBot = new SpamBot(1, TimeUnit.MILLISECONDS, new JID("echo@example.com"));
            AbstractBot gen = AnnotatedBotObject.from(genBot).get();
            connectionManager.addBot(gen, new JID("gen2@example.com"), mock);
        }

        {
            SpamBot genBot = new SpamBot(1, TimeUnit.MILLISECONDS, new JID("echo@example.com"));
            AbstractBot gen = AnnotatedBotObject.from(genBot).get();
            connectionManager.addBot(gen, new JID("gen3@example.com"), mock);
        }
        */

        connector.start();
        connectionManager.start();

        // start metrics reporting to JMX
        final JmxReporter reporter = JmxReporter.forRegistry(Meters.Metrics).build();
        reporter.start();

    }

}
