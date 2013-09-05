package botto.xmpp.engine;

import botto.xmpp.annotations.Context;
import botto.xmpp.annotations.Receive;

import botto.xmpp.botto.xmpp.connector.ConnectorId;
import botto.xmpp.connectors.mock.MockConnector;
import botto.xmpp.connectors.mock.MockConnectorConfiguration;
import botto.xmpp.service.dispatcher.AbstractBot;
import botto.xmpp.service.dispatcher.Meters;
import botto.xmpp.service.reflection.AnnotatedBotObject;
import com.codahale.metrics.JmxReporter;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MockEngineMain {

    public static void main(String[] args) throws Exception {

        ConnectionManager connectionManager = new ConnectionManager();

        MockConnectorConfiguration configuration = new MockConnectorConfiguration("example.com");
        configuration.setDomain("example.com");
        MockConnector connector = new MockConnector(configuration);

        ConnectorId mock = connectionManager.registerConnector(connector);

        // echo bot
        ExampleEngineMain.ExampleBot echoBot = new ExampleEngineMain.ExampleBot();
        AbstractBot echo = AnnotatedBotObject.from(echoBot).get();
        connectionManager.addBot(echo, new JID("echo@example.com"), mock);

        // add a generator bot
        {
            GenBot genBot = new GenBot(1, TimeUnit.MILLISECONDS, new JID("echo@example.com"));
            AbstractBot gen = AnnotatedBotObject.from(genBot).get();
            connectionManager.addBot(gen, new JID("gen@example.com"), mock);
        }

        {
            GenBot genBot = new GenBot(1, TimeUnit.MILLISECONDS, new JID("echo@example.com"));
            AbstractBot gen = AnnotatedBotObject.from(genBot).get();
            connectionManager.addBot(gen, new JID("gen2@example.com"), mock);
        }

        {
            GenBot genBot = new GenBot(1, TimeUnit.MILLISECONDS, new JID("echo@example.com"));
            AbstractBot gen = AnnotatedBotObject.from(genBot).get();
            connectionManager.addBot(gen, new JID("gen3@example.com"), mock);
        }

        connector.start();
        connectionManager.start();

        // start metrics reporting to JMX
        final JmxReporter reporter = JmxReporter.forRegistry(Meters.Metrics).build();
        reporter.start();

    }

    public static class GenBot implements Runnable {

        private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        private final JID dest;
        private int count = 0;

        @Context
        botto.xmpp.annotations.PacketOutput out;

        public GenBot(int delay, TimeUnit unit, JID dest) {
            this.dest = dest;
            executor.scheduleAtFixedRate(this, 0, delay, unit);
        }

        @Receive
        public void receive(Message message) {

        }

        @Override
        public void run() {
            Message message = new Message();
            message.setBody("Message #" + count);
            message.setTo(dest);
            out.send(message);
            count++;
        }
    }

}
