package botto.xmpp.botto.xmpp.connector;

public class ChannelEvent {

    private final Channel channel;
    private final ChannelStatus status;
    private final String message;
    private final Throwable error;

    public static ChannelEvent opening(Channel channel) {
        return opening(channel, null);
    }

    public static ChannelEvent opening(Channel channel, String message) {
        return new ChannelEvent(channel, ChannelStatus.Opening, message, null);
    }

    public static ChannelEvent opened(Channel channel) {
        return opened(channel, null);
    }

    public static ChannelEvent opened(Channel channel, String message) {
        return new ChannelEvent(channel, ChannelStatus.Opened, message, null);
    }

    public static ChannelEvent connecting(Channel channel) {
        return connecting(channel, null);
    }

    public static ChannelEvent connecting(Channel channel, String message) {
        return new ChannelEvent(channel, ChannelStatus.Connecting, message, null);
    }

    public static ChannelEvent connected(Channel channel, String message) {
        return new ChannelEvent(channel, ChannelStatus.Connected, message, null);
    }

    public static ChannelEvent connected(Channel channel) {
        return connected(channel, null);
    }

    public static ChannelEvent disconnected(Channel channel, String message) {
        return new ChannelEvent(channel, ChannelStatus.Disconnected, message, null);
    }

    public static ChannelEvent disconnected(Channel channel, String message, Throwable error) {
        return new ChannelEvent(channel, ChannelStatus.Disconnected, message, error);
    }

    public static ChannelEvent disconnecting(Channel channel, String message, Throwable error) {
        return new ChannelEvent(channel, ChannelStatus.Disconnected, message, error);
    }

    public static ChannelEvent disconnecting(Channel channel, String message) {
        return new ChannelEvent(channel, ChannelStatus.Disconnected, message, null);
    }

    public static ChannelEvent closing(Channel channel) {
        return new ChannelEvent(channel, ChannelStatus.Closing, null, null);
    }

    public static ChannelEvent closed(Channel channel) {
        return closed(channel, null, null);
    }

    public static ChannelEvent closed(Channel channel, String message) {
        return new ChannelEvent(channel, ChannelStatus.Opening, message, null);
    }

    public static ChannelEvent closed(Channel channel, String message, Throwable error) {
        return new ChannelEvent(channel, ChannelStatus.Closed, message, error);
    }

    private ChannelEvent(Channel channel, ChannelStatus status, String message, Throwable error) {
        this.channel = channel;
        this.status = status;
        this.message = message;
        this.error = error;
    }

    public ChannelStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getError() {
        return error;
    }

    public Channel getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return "ChannelEvent(" + channel + ", " + status + ", " + message + ")";
    }

}
