package botto.xmpp.utils;

import com.google.common.util.concurrent.FutureCallback;
import org.slf4j.Logger;

public class LoggingCallback<T> implements FutureCallback<T> {

    private final Logger logger;

    public LoggingCallback(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void onSuccess(T result) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onFailure(Throwable t) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
