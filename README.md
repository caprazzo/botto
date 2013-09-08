## Botto - XMPP Bot Service

[![Build Status](https://travis-ci.org/mcaprari/botto.png?branches=snapshot)](https://travis-ci.org/mcaprari/botto)
[![Coverage Status](https://coveralls.io/repos/mcaprari/botto/badge.png?branches=snapshot)](https://coveralls.io/r/mcaprari/botto)

Botto is a Java framework for easy development on XMPP Bots


Botto has out-of-the-box support for:
* annotation-only pojo bots
* multiple single-bot per instance (bot@example.com)
* multiple bot-component per instance (bot@component.example.com)

For a working example, see: https://github.com/mcaprari/botto/blob/master/botto-service/src/test/java/net/caprazzi/xmpp/EchoBotService.java

For integration with Dropwizard, see: https://github.com/mcaprari/botto/tree/master/botto-examples/botto-examples-dropwizard

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
            <version>0.6.2</version>
        </dependency>
    </dependencies>
```

Create a new botService:

```java
public class EchoBotService extends AbstractBotService {

    public static void main(String[] main) {
        BotServiceConfiguration configuration = new BotServiceConfiguration();
        configuration.setHost("localhost");
        configuration.setPort(5275);
        configuration.setSecret("secret");
        new EchoBotService().run(configuration);
    }

    @Override
    public void run(ServiceEnvironment environment) {
        EchoBot echoBot = new EchoBot();

        // setup echo bot to listen at echo@subdomain1.yourdomain.com
        SubdomainEnvironment subdomain = environment.getSubdomain("subdomain1");
        subdomain.addBot(echoBot, "echo");

        RelayBot relayBot = new RelayBot();

        // setup echo bot to listen at relay@subdomain2.yourdomain.com
        SubdomainEnvironment subdomain2 = environment.getSubdomain("subdomain2");
        subdomain2.addBot(relayBot, "relay");

        // setup relay bot to listen at echo@yourdomain.com
        // (you need to create a matching user in your xmpp server)
        BotEnvironment echoSingleBotEnv = environment.getBot("echo");
        echoSingleBotEnv.setBot(echoBot);
        echoSingleBotEnv.setSecret("secret");
    }
}
```

Echo Bot:

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

Relay Bot:

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





