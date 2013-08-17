package botto.xmpp.service.utils;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AbstractFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * Abstract single-threaded async queue executor.
 */
public abstract class QueueExecutor<TElement> {

    private final Logger Log;

    private final BlockingQueue<ElementListenableFuture<TElement>> queue = new LinkedBlockingQueue<ElementListenableFuture<TElement>>();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    protected QueueExecutor() {
        Log = LoggerFactory.getLogger(this.getClass());
    }

    public abstract void doProcess(TElement element);

    public final void start() {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                while(!Thread.currentThread().isInterrupted() && !executor.isShutdown()) {
                    try {
                        ElementListenableFuture<TElement> element = queue.take();
                        if (element == null) {

                            if (Log.isTraceEnabled())
                                Log.trace("Element is null, not processing");

                            continue;
                        }

                        if (Log.isTraceEnabled())
                            Log.trace("Processing element {}", element);

                        try {
                            doProcess(element.getElement());
                            element.setSuccess();
                        }
                        catch (Exception ex) {
                            Log.error("Error while processing element {}: {}", element, ex);
                            element.setException(ex);
                        }
                    } catch (InterruptedException e) {
                        // it's ok, we are shutting down now
                        break;
                    }
                }
            }
        });
    }

    public final ElementListenableFuture enqueue(TElement element) {
        Preconditions.checkNotNull(element, "Can't enqueue null elements");

        if (Log.isDebugEnabled())
            Log.debug("Adding element to queue: {}", element);

        ElementListenableFuture future = new ElementListenableFuture(element);

        try {
            queue.put(future);
        } catch (InterruptedException e) {
            Log.warn("Interrupted Exception while adding an element to internal queue.");
            Thread.currentThread().interrupt();
        }

        return future;
    }

    public final void shutdown() {
        // Disable new tasks from being submitted
        executor.shutdown();
        try {
            // Wait a while for existing tasks to terminate
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // Cancel currently executing tasks

                // Wait a while for tasks to respond to being cancelled
                if (!executor.awaitTermination(2, TimeUnit.SECONDS))
                    Log.warn("Executor did not terminate cleanly");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            executor.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }

        Log.info("Shut down complete");
    }

    public static class ElementListenableFuture<TElement> extends AbstractFuture<Boolean> {
        private final TElement element;

        public static <TElement> ElementListenableFuture<TElement> create(TElement element) {
            return new ElementListenableFuture(element);
        }

        private ElementListenableFuture(TElement element) {
            this.element = element;
        }

        public TElement getElement() {
            return element;
        }

        public void setSuccess() {
            this.set(true);
        }

        public void setException(Exception ex) {
            this.setException(ex);
        }
    }
}
