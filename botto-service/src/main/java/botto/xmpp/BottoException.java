package botto.xmpp;

import net.caprazzi.reusables.common.FormattedException;

public class BottoException extends FormattedException {
    public BottoException(Throwable t) {
        super(t);
    }

    public BottoException(Throwable t, String message, Object... params) {
        super(t, message, params);
    }

    public BottoException(String message, Object... params) {
        super(message, params);
    }
}
