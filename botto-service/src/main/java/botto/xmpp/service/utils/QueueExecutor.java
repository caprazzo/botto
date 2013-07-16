package botto.xmpp.service.utils;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Abstract single-threaded async queue executor.
 * TODO: add ability to pause the deque thread when doProcess can't process
 * TODO: implement graceful shutdown with timeout
 */
public abstract class QueueExecutor<T> {

    private final Logger Log = LoggerFactory.getLogger(QueueExecutor.class);

    private final BlockingQueue<T> outbox = new LinkedBlockingQueue<T>();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public abstract void doProcess(T element);

    public final void start() {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                while(!Thread.currentThread().isInterrupted()) {
                    // TODO: consider using drain to deque multiple items
                    try {
                        T element = outbox.take();
                        if (element == null) {
                            Log.warn("Element is null, not processing");
                            return;
                        }
                        Log.trace("Processing element {}", element);
                        try {
                            doProcess(element);
                        }
                        catch (Exception ex) {
                            Log.error("Error while processing element {}: {}", element, ex);
                        }
                    } catch (InterruptedException e) {
                        // it's ok, we are shutting down now
                    }
                }
            }
        });
    }

    public final void enqueue(T element) {
        Preconditions.checkNotNull(element, "Can't equeue null elements");
        try {
            outbox.put(element);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public final void shutdown() {
        executor.shutdownNow();
        Log.info("Shut down complete");
    }
}
