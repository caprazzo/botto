package botto.xmpp.botto.xmpp.connector.channel;

public enum ChannelStatus {
    Opening,
    Opened,
    Connecting,
    Connected,
    Disconnecting,
    Disconnected,
    Closing, Closed;

    public boolean isConnected() {
        return this == Connected;
    }
}
