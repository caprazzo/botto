package botto.xmpp;

import botto.xmpp.botto.xmpp.connector.Channel;
import com.google.common.base.Preconditions;
import org.xmpp.packet.JID;

import java.util.concurrent.ConcurrentHashMap;

class ChannelRegistry {

    private final ConcurrentHashMap<JID, ChannelBot> channels = new ConcurrentHashMap<JID, ChannelBot>();

    public void addChannel(Channel channel, AbstractBot bot) {
        Preconditions.checkNotNull(channel);
        Preconditions.checkNotNull(bot);
        ChannelBot found = channels.putIfAbsent(channel.getAddress(), new ChannelBot(channel, bot));
        if (found != null) {
            throw new BottoRuntimeException("There is already a bot for channel {}: {}", channel, bot);
        }
    }

    public void removeChannel(Channel channel) {
        Preconditions.checkNotNull(channel);
        ChannelBot removed = channels.remove(channel.getAddress());
        if (removed == null) {
            throw new BottoRuntimeException("Could not remove bot for channel {}: not found", channel);
        }
    }

    public Channel getChannel(JID address) {
        Preconditions.checkNotNull(address);
        ChannelBot channelBot = channels.get(address);
        if (channelBot != null) {
            return channelBot.getChannel();
        }
        return null;
    }

    public AbstractBot getBot(Channel channel) {
        Preconditions.checkNotNull(channel);
        ChannelBot channelBot = channels.get(channel.getAddress());
        if (channelBot != null) {
            return channelBot.getBot();
        }
        return null;
    }

    private static class ChannelBot {
        private final Channel channel;
        private final AbstractBot bot;

        public ChannelBot(Channel channel, AbstractBot bot) {
            this.channel = channel;
            this.bot = bot;
        }

        private Channel getChannel() {
            return channel;
        }

        private AbstractBot getBot() {
            return bot;
        }
    }

}
