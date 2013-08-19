package botto.xmpp.engine;

import botto.xmpp.annotations.ConnectionInfo;

public interface ConnectionInfoListener {
    public void onConnectionInfo(ConnectionInfo info);
}
