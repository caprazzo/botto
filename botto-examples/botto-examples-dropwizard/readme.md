# Example of Botto / Dropwizard integration

This example project demonstrates how to:

- configure a BotService using Dropwizard configuration
- create and configure a new Bot
- create a REST Resource that can use the bot to send XMPP messages
- start a bot service instance inside Dropwizard


SayHelloBot:

```java
/**
 * A bot that can say hello to an arbitrary XMPP user
 */
public class SayHelloBot {

    @Context
    private PacketOutput output;

    @Context
    private ConnectionInfo botContext;

    public boolean isConnected() {
        return botContext.isConnected();
    }

    /**
     * Sends an XMPP message that says 'hello'
     * @param dest a valid user JID
     */
    public void sayHello(String dest) {
        Message message = new Message();
        message.setTo(new JID(dest));
        message.setBody("Hello, " + dest);
        message.setFrom("hello");
        output.send(message);
    }
}

```

SayHelloResource:
```java
/**
 * A simple resource that sends a greeting to
 * an XMPP user via the SayHelloBot
 */
@Path("/user")
public class SayHelloResource {

    private final SayHelloBot helloBot;

    public SayHelloResource(SayHelloBot helloBot) {
        this.helloBot = helloBot;
    }

    @GET
    @Path("/check")
    public boolean isConnected() {
        return helloBot.isConnected();
    }

    @POST
    @Path("/hello/{jid}/")
    public void sayHello(@PathParam("jid") String toJid) {
        helloBot.sayHello(toJid);
    }
}
```

Dropwizrd Configuration Class:
```java
/**
 * To configure a BotService from a Dropwizard configuration,
 * simply add BotServiceConfiguration as JsonProperty
 *
 * See botto-dropwizard-service.yml for how to add values to the configuration
 */
public class BottoDropwizardServiceConfiguration extends Configuration {

    @JsonProperty
    private BotServiceConfiguration botService = new BotServiceConfiguration();

    public BotServiceConfiguration getBotService( ){
        return botService;
    }
}
```

Putting it all together:

```java
public void run(BottoDropwizardServiceConfiguration dropwizardConfig, Environment dropwizardEnvironment) throws Exception {

    // create an instance of the simple Bot
    final SayHelloBot bot = new SayHelloBot();

    // create an instance of the simple resource and reference the bot
    SayHelloResource resource = new SayHelloResource(bot);

    // configure the resource
    dropwizardEnvironment.addResource(resource);

    // create a botservice instance
    AbstractBotService botService = new AbstractBotService() {
        @Override
        protected void run(ServiceEnvironment environment) {
            // create a new xmpp subdomain
            SubdomainEnvironment subdomain = environment.getSubdomain("dropwizard");

            // add the bot to this subdomain, and assigne the node "hello" to it
            // so that bot will be addressable as hello@dropwizard.example.com
            subdomain.addBot(bot, "hello");
        }
    };

    // obtain bot service config from dropwizard config
    BotServiceConfiguration botServiceConfiguration = dropwizardConfig.getBotService();

    // run the service
    botService.run(botServiceConfiguration);
}
```