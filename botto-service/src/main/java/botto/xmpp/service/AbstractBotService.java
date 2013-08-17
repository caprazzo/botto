package botto.xmpp.service;

import botto.xmpp.service.bot.BotSessionManager;
import botto.xmpp.service.component.ComponentBotExecutor;
import botto.xmpp.service.component.ComponentBotRouter;
import botto.xmpp.service.component.ComponentPacketSender;
import botto.xmpp.service.component.PacketRoutingComponent;
import botto.xmpp.annotations.PacketOutput;
import org.jivesoftware.whack.ExternalComponentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.component.Component;
import org.xmpp.component.ComponentException;
import org.xmpp.packet.Packet;

import java.net.ConnectException;
import java.util.concurrent.Executors;

public abstract class AbstractBotService {

    private final Logger Log = LoggerFactory.getLogger(AbstractBotService.class);

    private ServiceEnvironment environment;
    private ComponentPacketSender sender;
    private ExternalComponentManager manager;
    private ComponentBotExecutor botExecutor;
    private BotSessionManager botSessionManager;

    public final void run(BotServiceConfiguration configuration) {
        environment = new ServiceEnvironment(configuration);
        run(environment);

        manager = new ExternalComponentManager(configuration.getHost(), configuration.getComponentPort());
        sender = new ComponentPacketSender(manager);


        botExecutor = new ComponentBotExecutor(Executors.newCachedThreadPool());
        botSessionManager = new BotSessionManager(configuration.getHost(), configuration.getClientPort());

        ComponentBotRouter router = new ComponentBotRouter(botExecutor);

        // setup sub-domains
        for(SubdomainEnvironment subdomain : environment.getSubdomains()) {

            // create a component for each subdomain
            final Component component = new PacketRoutingComponent(router, subdomain.getName());
            final BotConnectionInfo connectionInfo = new BotConnectionInfo();

            PacketOutput output = new PacketOutput() {
                @Override
                public void send(Packet packet) {
                    sender.send(component, packet);
                }
            };

            for(SubdomainBotEnvironment holder : subdomain.getBots()) {
                holder.getBot().setPacketOutput(output);
                holder.getBot().setConnectionInfo(connectionInfo);
                router.addBot(holder.getBot(), subdomain.getName(), holder.getNodeFilter());
            }

            try {
                manager.setSecretKey(subdomain.getName(), subdomain.getSecret());
                manager.setMultipleAllowed(subdomain.getName(), true);
                manager.removeComponent(subdomain.getName());
                manager.addComponent(subdomain.getName(), component);
                connectionInfo.setConnectionStatus(true);
                Log.info("Components Connected");
            } catch (ComponentException e) {
                if (e.getCause() instanceof ConnectException) {
                    Log.error("Could not connect to {}:{} while configuring component for subdomain {}",
                            configuration.getHost(), configuration.getClientPort(), subdomain.getName());

                }
                stop();
                throw new RuntimeException(e);
            }
        }

        // setup single bots
        for(BotEnvironment botEnv : environment.getBots()) {
            botSessionManager.createSession(botEnv.getBot(), botEnv.getNode(), botEnv.getSecret(), botEnv.getResource());
        }

        sender.start();
        botSessionManager.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                Log.info("Running shutdown hook");
                stop();
            }
        }));
    }

    public void stop() {

        Log.info("Shutting Down");

        if (sender != null)
            sender.stop();

        if (botExecutor != null)
            botExecutor.stop();

        if (botSessionManager != null)
            botSessionManager.stop();

        if (environment != null) {
            for(SubdomainEnvironment subdomain : environment.getSubdomains()) {
                try {
                    subdomain.shutdown();
                    if (manager != null)
                        manager.removeComponent(subdomain.getName());
                } catch (ComponentException e) {
                    Log.warn("Component exception during shutdown: {}", e);
                }
            }
        }

        Log.info("Shutdown complete");
    }

    protected abstract void run(ServiceEnvironment environment);
}
