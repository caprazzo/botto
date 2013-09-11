package botto.xmpp;

import botto.xmpp.annotations.BotContext;
import botto.xmpp.botto.xmpp.connector.channel.ChannelContext;
import botto.xmpp.botto.xmpp.connector.channel.ChannelStatus;

public class ChannelBotContext implements BotContext {

    private volatile ChannelContext context;

    public ChannelBotContext(ChannelContext context) {
        this.context = context;
    }

    public void setChannelContext(ChannelContext context) {
        this.context = context;
    }

    @Override
    public boolean isConnected() {
        return context != null && context.getStatus() == ChannelStatus.Connected;
    }
}
