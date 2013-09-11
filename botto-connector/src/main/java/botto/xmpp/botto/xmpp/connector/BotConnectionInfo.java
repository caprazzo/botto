package botto.xmpp.botto.xmpp.connector;

import botto.xmpp.annotations.BotContext;

import java.util.concurrent.atomic.AtomicBoolean;

public final class BotConnectionInfo implements BotContext {

    private final AtomicBoolean isConnected = new AtomicBoolean();

    public void setConnectionStatus(boolean isConnected) {
        this.isConnected.set(isConnected);
    }

    @Override
    public boolean isConnected() {
        return this.isConnected.get();
    }
}
