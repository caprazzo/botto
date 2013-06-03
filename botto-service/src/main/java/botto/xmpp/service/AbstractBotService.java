package botto.xmpp.service;

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

import java.util.concurrent.Executors;

public abstract class AbstractBotService {

    private final Logger Log = LoggerFactory.getLogger(AbstractBotService.class);

    public final void run(BotServiceConfiguration configuration) {
        final ServiceEnvironment environment = new ServiceEnvironment(configuration);
        run(environment);

        final ExternalComponentManager manager = new ExternalComponentManager(configuration.getHost(), configuration.getPort());
        final ComponentPacketSender sender = new ComponentPacketSender(manager);

        final ComponentBotExecutor botExecutor = new ComponentBotExecutor(Executors.newFixedThreadPool(10));
        ComponentBotRouter router = new ComponentBotRouter(botExecutor);

        for(SubdomainEnvironment subdomain : environment.getSubdomains()) {

            // create a component for each subdomain
            final Component component = new PacketRoutingComponent(router, subdomain.getName());

            PacketOutput output = new PacketOutput() {
                @Override
                public void send(Packet packet) {
                    sender.send(component, packet);
                }
            };

            for(SubdomainBotEnvironment holder : subdomain.getBots()) {
                holder.getBot().setPacketOutput(output);
                router.addBot(holder.getBot(), subdomain.getName(), holder.getNodeFilter());
            }

            try {
                manager.setSecretKey(subdomain.getName(), subdomain.getSecret());
                manager.setMultipleAllowed(subdomain.getName(), true);
                manager.removeComponent(subdomain.getName());
                manager.addComponent(subdomain.getName(), component);
                Log.info("Connected");
            } catch (ComponentException e) {
                throw new RuntimeException(e);
            }
        }

        sender.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                Log.info("Running shutdown hook");
                sender.shutdown();
                botExecutor.shutdown();

                for(SubdomainEnvironment subdomain : environment.getSubdomains()) {
                    try {
                        manager.removeComponent(subdomain.getName());
                    } catch (ComponentException e) {
                        Log.warn("Component exception during shutdown: {}", e);
                    }
                }
            }
        }));
    }

    protected abstract void run(ServiceEnvironment environment);
}
