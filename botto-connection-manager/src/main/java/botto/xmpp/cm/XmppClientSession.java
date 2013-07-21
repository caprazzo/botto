package botto.xmpp.cm;

public class XmppClientSession {

    private final String username;
    private final XmppConnectionManager manager;

    public XmppClientSession(String username, XmppConnectionManager manager) {
        this.username = username;
        this.manager = manager;
    }

    public void connect() {

    }

    public void login(String username, String password) {

    }

    public void onConnected() {

    }

    public void onLogin() {
        // send out a message
    }
}
