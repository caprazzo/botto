package botto.xmpp.service.utils;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * Abstract single-threaded async queue executor.
 */
public abstract class QueueExecutor<TElement> {

    private final Logger Log;

    private final BlockingQueue<ElementHolder<TElement>> outbox = new LinkedBlockingQueue<ElementHolder<TElement>>();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    protected QueueExecutor() {
        Log = LoggerFactory.getLogger(this.getClass());
    }

    public abstract void doProcess(TElement element);

    public final void start() {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                while(!Thread.currentThread().isInterrupted()) {
                    try {
                        ElementHolder<TElement> element = outbox.take();
                        if (element == null) {

                            if (Log.isTraceEnabled())
                                Log.trace("Element is null, not processing");

                            continue;
                        }

                        if (Log.isTraceEnabled())
                            Log.trace("Processing element {}", element);

                        try {
                            doProcess(element.getElement());
                        }
                        catch (Exception ex) {
                            Log.error("Error while processing element {}: {}", element, ex);
                        }
                    } catch (InterruptedException e) {
                        // it's ok, we are shutting down now
                        break;
                    }
                }
            }
        });
    }

    public final void enqueue(TElement element) {
        Preconditions.checkNotNull(element, "Can't enqueue null elements");

        if (Log.isDebugEnabled())
            Log.debug("Adding element to queue: {}", element);

        try {
            outbox.put(new ElementHolder(element));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public final void shutdown() {
        // TODO: stop accepting new items
        // TODO: run until the queue is empty or a timeout has elapsed
        executor.shutdownNow();
        Log.info("Shut down complete");
    }

    private static class ElementHolder<TElement> {
        private final TElement element;

        public ElementHolder(TElement element) {
            this.element = element;
        }

        public TElement getElement() {
            return element;
        }
    }
}
