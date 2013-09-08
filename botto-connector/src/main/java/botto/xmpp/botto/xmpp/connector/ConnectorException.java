package botto.xmpp.botto.xmpp.connector;

import net.caprazzi.reusables.common.FormattedException;

public class ConnectorException extends FormattedException {

    public ConnectorException(Throwable t) {
        super(t);
    }

    public ConnectorException(Throwable t, String message, Object... params) {
        super(t, message, params);
    }

    public ConnectorException(String message, Object... params) {
        super(message, params);
    }
}
