package botto.xmpp;

import botto.xmpp.annotations.BotContext;
import botto.xmpp.botto.xmpp.connector.Connector;
import botto.xmpp.botto.xmpp.connector.channel.ChannelContext;
import botto.xmpp.botto.xmpp.connector.channel.ChannelStatus;
import com.google.common.base.Preconditions;
import org.xmpp.packet.Packet;

public class ChannelBotContext implements BotContext {

    private final ChannelContext context;
    private final BotManager manager;
    private final Connector connector;

    public ChannelBotContext(ChannelContext context, BotManager manager, Connector connector) {
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(manager);
        Preconditions.checkNotNull(connector);
        this.context = context;
        this.manager = manager;
        this.connector = connector;
    }

    @Override
    public boolean isConnected() {
        return context != null && context.getStatus() == ChannelStatus.Connected;
    }

    @Override
    public void send(Packet packet) {
        manager.send(connector, context.getChannel(), packet);
    }
}
