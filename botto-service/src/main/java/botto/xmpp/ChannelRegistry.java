package botto.xmpp;

import botto.xmpp.botto.xmpp.connector.channel.Channel;
import botto.xmpp.botto.xmpp.connector.channel.ChannelContext;
import botto.xmpp.botto.xmpp.connector.channel.ChannelContextListener;
import botto.xmpp.botto.xmpp.connector.channel.ChannelEvent;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.event.EventListenerSupport;
import org.xmpp.packet.JID;

import java.util.concurrent.ConcurrentHashMap;

class ChannelRegistry {

    // TODO: only use address.toBareJID for indexing
    private final ConcurrentHashMap<JID, ChannelBot> channels = new ConcurrentHashMap<JID, ChannelBot>();

    private final EventListenerSupport<ChannelContextListener> listeners =
            EventListenerSupport.create(ChannelContextListener.class);

    public void addChannel(ChannelContext context, AbstractBot bot) {
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(context.getChannel());
        Preconditions.checkNotNull(context.getChannel().getAddress());
        Preconditions.checkNotNull(bot);
        ChannelBot found = channels.putIfAbsent(context.getChannel().getAddress(), new ChannelBot(context, bot));
        if (found != null) {
            throw new BottoRuntimeException("There is already a bot for channel {0}: {1}", context, bot);
        }
    }

    public void removeChannel(Channel channel) {
        Preconditions.checkNotNull(channel);
        ChannelBot removed = channels.remove(channel.getAddress());
        if (removed == null) {
            throw new BottoRuntimeException("Could not remove bot for channel {0}: not found", channel);
        }
    }

    public Channel getChannel(JID address) {
        Preconditions.checkNotNull(address);
        ChannelBot channelBot = channels.get(address);
        if (channelBot != null) {
            return channelBot.getContext().getChannel();
        }
        throw new BottoRuntimeException("No channel found for address {0}", address);
    }

    public AbstractBot getBot(Channel channel) {
        Preconditions.checkNotNull(channel);
        ChannelBot channelBot = channels.get(channel.getAddress());
        if (channelBot != null) {
            return channelBot.getBot();
        }
        return null;
    }

    public AbstractBot getBot(JID address) {
        Preconditions.checkNotNull(address);
        ChannelBot channelBot = channels.get(address);
        if (channelBot != null) {
            return channelBot.getBot();
        }
        return null;
    }

    public void setChannelEvent(ChannelEvent event) {
        Preconditions.checkNotNull(event);
        ChannelBot entry = channels.get(event.getChannel().getAddress());
        if (entry == null) {
            return;
        }
        entry.getContext().setEvent(event);
        listeners.fire().onChannelEvent(entry.getContext(), event);
    }

    public void addChannelContextListener(ChannelContextListener listener) {
        listeners.addListener(listener);
    }

    private static class ChannelBot {
        private final ChannelContext context;
        private final AbstractBot bot;

        public ChannelBot(ChannelContext context, AbstractBot bot) {
            this.context = context;
            this.bot = bot;
        }

        private ChannelContext getContext() {
            return context;
        }

        private AbstractBot getBot() {
            return bot;
        }
    }

}
