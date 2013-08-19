package botto.xmpp.service.dispatcher;

import com.google.common.util.concurrent.AbstractFuture;

public class ListenableConfirmation extends AbstractFuture<Boolean> {

    public void setSuccess() {
        set(true);
    }

    public void setFailure(Throwable ex) {
        setException(ex);
    }

    public static ListenableConfirmation failed(RuntimeException e) {
        ListenableConfirmation confirmation = new ListenableConfirmation();
        confirmation.setException(e);
        return confirmation;
    }

    public static ListenableConfirmation create() {
        return new ListenableConfirmation();
    }
}
