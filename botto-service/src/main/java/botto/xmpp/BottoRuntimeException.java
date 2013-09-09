package botto.xmpp;

import net.caprazzi.reusables.common.FormattedRuntimeException;

public class BottoRuntimeException extends FormattedRuntimeException {

    public BottoRuntimeException(Throwable t) {
        super(t);
    }

    public BottoRuntimeException(Throwable t, String message, Object... params) {
        super(t, message, params);
    }

    public BottoRuntimeException(String message, Object... params) {
        super(message, params);
    }
}
