package botto.xmpp.engine;

import botto.xmpp.annotations.ConnectionInfo;

import java.util.concurrent.atomic.AtomicBoolean;

public final class BotConnectionInfo implements ConnectionInfo {

    private final AtomicBoolean isConnected = new AtomicBoolean();

    public void setConnectionStatus(boolean isConnected) {
        this.isConnected.set(isConnected);
    }

    @Override
    public boolean isConnected() {
        return this.isConnected.get();
    }
}
