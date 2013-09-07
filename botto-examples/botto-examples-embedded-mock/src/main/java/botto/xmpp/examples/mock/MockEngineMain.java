package botto.xmpp.examples.mock;

import botto.xmpp.BottoConnectionManager;

import botto.xmpp.botto.xmpp.connector.ConnectorId;
import botto.xmpp.connectors.mock.MockConnector;
import botto.xmpp.connectors.mock.MockConnectorConfiguration;
import botto.xmpp.AbstractBot;
import botto.xmpp.Meters;

import botto.xmpp.examples.bots.EchoBot;
import botto.xmpp.examples.bots.SpamBot;
import botto.xmpp.service.reflection.AnnotatedBotObject;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.codahale.metrics.JmxReporter;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;

import java.util.concurrent.TimeUnit;

public class MockEngineMain {

    public static void main(String[] args) throws Exception {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);

        BottoConnectionManager connectionManager = new BottoConnectionManager();

        MockConnectorConfiguration configuration = new MockConnectorConfiguration("example.com");
        configuration.setDomain("example.com");
        MockConnector connector = new MockConnector(configuration);

        ConnectorId mock = connectionManager.registerConnector(connector);

        // echo bot
        EchoBot echoBot = new EchoBot();
        AbstractBot echo = AnnotatedBotObject.from(echoBot).get();
        connectionManager.addBot(echo, new JID("echo@example.com"), mock);

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
