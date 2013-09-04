package botto.xmpp.botto.xmpp.connector;

import net.caprazzi.reusables.common.FormattedException;

public class ConnectorException extends FormattedException {

    public ConnectorException(Exception ex) {
        super(ex);
    }

    public ConnectorException(Exception ex, String message, String... params) {
        super(ex, message, params);
    }

    public ConnectorException(String message, Object... params) {
        super(message, params);
    }
}
