package botto.xmpp.botto.xmpp.connector.channel;

public class ChannelContext {
    private final Channel channel;
    private ChannelStatus status = ChannelStatus.Opening;

    public static ChannelContext of(Channel channel) {
        return new ChannelContext(channel);
    }

    private ChannelContext(Channel channel) {
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    }

    public ChannelStatus getStatus() {
        return status;
    }

    public void setEvent(ChannelEvent event) {
        status = event.getStatus();
    }
}
