## Botto - XMPP Infrastructure Components

[![Build Status](https://travis-ci.org/mcaprari/botto.png)](https://travis-ci.org/mcaprari/botto)
[![Coverage Status](https://coveralls.io/repos/mcaprari/botto/badge.png?)](https://coveralls.io/r/mcaprari/botto)


Botto is a collection of components to build messaging infrastructures over XMPP.

Botto has out-of-the-box support for:
* annotation-only pojo bots
* multiple single-bot per instance (bot@example.com)
* multiple bot-component per instance (bot@component.example.com)
* connection to multiple XMPP servers per instance
* Deep realtime metrics via JMX

For a working example, see: https://github.com/mcaprari/botto/blob/master/botto-examples/botto-examples-embedded/src/main/java/botto/xmpp/examples/embedded/server/ExampleEmbeddedBotServer.java

### Quick start:

Import using maven:

```xml
    <repositories>
        <repository>
            <id>mcaprari-releases</id>
            <url>https://github.com/mcaprari/mcaprari-maven-repo/raw/master/releases</url>
        </repository>
        <repository>
            <id>mcaprari-snapshots</id>
            <url>https://github.com/mcaprari/mcaprari-maven-repo/raw/master/snapshots</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>botto.xmpp</groupId>
            <artifactId>botto-service</artifactId>
            <version>0.7.0-SNAPSHOT</version>
        </dependency>
    </dependencies>
```

Write your first embbedded bot system:

```java
public class ExampleEmbeddedBotServer {    
    public static void main(String[] args) throws BottoException {
        // start the JMX reporter - use Visual VM to see a number of runtime metrics
        final JmxReporter reporter = com.codahale.metrics.JmxReporter.forRegistry(Meters.Metrics).build();
        reporter.start();
            
        BotManager botManager = BotManager.create();

        // listen to all changes to all channel events
        botManager.addChannelEventListener(new ChannelContextListener() {
            @Override
            public void onChannelEvent(ChannelContext context, ChannelEvent event) {
                Log.info("Event {} for {}", event, context);
            }
        });       

        // create a new connector
        SmackConnectorConfiguration smackConfiguration = new SmackConnectorConfiguration();
        smackConfiguration.setHost("localhost");
        smackConfiguration.setPort(5222);
        smackConfiguration.setSecret("secret");

        SmackConnector smackConnector = new SmackConnector(smackConfiguration);

        // activate the connector
        ConnectorId connectorId = botManager.registerConnector(smackConnector);

        JID echoAddress = new JID("echo@caprazzi.net");
        JID spamAddress = new JID("spam@caprazzi.net");

        // create a concrete bot from
        // an annotated spam bot (see https://github.com/mcaprari/botto/blob/master/botto-examples/botto-examples-bots/src/main/java/botto/xmpp/examples/bots/SpamBot.java)
        SpamBot spamBot = new SpamBot(echoAddress);
        AbstractBot spamAnnotatedBot = AnnotatedBotObject.from(spamBot).get();

        botManager.addBot(connectorId, spamAddress, spamAnnotatedBot);

        // create a concrete bot from
        // an annotated echo bot (see https://github.com/mcaprari/botto/blob/master/botto-examples/botto-examples-bots/src/main/java/botto/xmpp/examples/bots/EchoBot.java)
        EchoBot echoBot = new EchoBot();
        AbstractBot echo = AnnotatedBotObject.from(echoBot).get();
        botManager.addBot(connectorId, echoAddress, echo);

        botManager.start();

        // every second, execute the SpamBot
        service.scheduleAtFixedRate(spamBot, 0, 1, TimeUnit.SECONDS);
    }
}
```

## Echo Bot Example:

```java
/**
 * Simple bot echoes back any received message
 */
public class EchoBot {
    @Receive
    public Message echo(Message msg) {
        Message reply = new Message();
        reply.setTo(msg.getFrom());
        reply.setFrom(msg.getTo());
        reply.setBody("You said xx: " + msg.getBody());
        return reply;
    }
}
```

## Relay Bot Example:

```java
/**
 * Simple bot that relays any received message
 * to multiple addresses
 */
public class RelayBot {

    @Context
    private PacketOutput output;

    @Receive
    public void Receive(Message msg) {
        Message relayOne = new Message();
        relayOne.setFrom("relay");
        relayOne.setTo(new JID("bigbrother@example.com"));
        relayOne.setBody(msg.getFrom() + " just said " + msg.getBody());

        output.send(relayOne);

        Message relayTwo = new Message();
        relayOne.setFrom("relay");
        relayTwo.setTo(new JID("bigsister@example.com"));
        relayTwo.setBody(msg.getFrom() + " just said " + msg.getBody());

        output.send(relayTwo);
    }
}
```







[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/mcaprari/botto/trend.png)](https://bitdeli.com/free "Bitdeli Badge")

