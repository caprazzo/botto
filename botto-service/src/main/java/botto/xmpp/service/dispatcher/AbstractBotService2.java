package botto.xmpp.service.dispatcher;

import botto.xmpp.annotations.PacketOutput;
import botto.xmpp.service.*;
import botto.xmpp.service.bot.BotSessionManager;
import botto.xmpp.service.bot.PacketInputOutput;
import botto.xmpp.service.component.AbstractInterceptComponent;
import botto.xmpp.service.component.PacketRoutingComponent;
import org.jivesoftware.whack.ExternalComponentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.component.Component;
import org.xmpp.packet.Packet;

public abstract class AbstractBotService2 {

    private final Logger Log = LoggerFactory.getLogger(AbstractBotService2.class);

    private ServiceEnvironment environment;
    private DispatcherService dispatcher;

    public final void run(BotServiceConfiguration configuration) {
        environment = new ServiceEnvironment(configuration);
        run(environment);

        dispatcher = new DispatcherService();

        final ExternalComponentManager manager = new ExternalComponentManager(configuration.getHost(), configuration.getComponentPort());

        for(SubdomainEnvironment subdomain : environment.getSubdomains()) {

            final AbstractInterceptComponent component = new AbstractInterceptComponent(subdomain.getName()) {
                public PacketSourceListener listener;
                @Override
                public void processPacket(Packet packet) {
                    listener.receive(packet);
                }

                @Override
                public void setPacketSourceListener(PacketSourceListener listener) {
                    this.listener = listener;
                }
            };

            for(final SubdomainBotEnvironment botEnv : subdomain.getBots()) {
                PacketOutput output = new PacketOutput() {
                    @Override
                    public void send(Packet packet) {
                        // TODO: enforce sender address
                        packet.setFrom(botEnv.getNode());
                        manager.sendPacket(component, packet);
                    }
                };
                //dispatcher.addComponentBot(botEnv.getBot(), botEnv.getNodeFilter(), component, output);
            }
        }

        BotSessionManager botSessionManager = new BotSessionManager(configuration.getHost(), configuration.getClientPort());

        for (BotEnvironment env: environment.getBots()) {
            PacketInputOutput session = botSessionManager.createSession(env);
            //dispatcher.addNodeBot(env.getBot(), session.getSource(), session.getOutput());
        }

        dispatcher.start();
    }

    protected abstract void run(ServiceEnvironment environment);
}
