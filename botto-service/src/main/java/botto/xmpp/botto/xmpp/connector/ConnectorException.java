package botto.xmpp.botto.xmpp.connector;

public class ConnectorException extends Exception {
    public ConnectorException(String message, Throwable t) {
        super(message, t);
    }

    public ConnectorException(Throwable throwable) {
        super(throwable);
    }
}
