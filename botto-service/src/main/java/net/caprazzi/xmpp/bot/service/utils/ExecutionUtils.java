package net.caprazzi.xmpp.bot.service.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutionUtils {

    private final static Logger Log = LoggerFactory.getLogger(ExecutionUtils.class);

    public static void shutdown(ExecutorService executor, long timeout, TimeUnit unit) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(timeout, unit)) {
                Log.warn("Executor did not terminate in the specified time.");
                List<Runnable> droppedTasks = executor.shutdownNow();
                Log.warn("Executor was abruptly shut down. " + droppedTasks.size() + " tasks will not be executed.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
