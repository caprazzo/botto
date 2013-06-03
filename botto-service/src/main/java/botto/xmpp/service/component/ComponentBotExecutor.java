package botto.xmpp.service.component;

import com.google.common.util.concurrent.*;
import botto.xmpp.service.Bot;
import botto.xmpp.service.utils.ExecutionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.Packet;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Asynchronous executor of Bot receive() methods
 * TODO: can simply be BotExecutor
 */
public class ComponentBotExecutor {

    private final Logger Log = LoggerFactory.getLogger(ComponentBotExecutor.class);

    private final ListeningExecutorService executorService;

    public ComponentBotExecutor(ExecutorService executorService) {
        this.executorService = MoreExecutors.listeningDecorator(executorService);
    }

    public void execute(final Bot processor, final Packet packet) {

        if (executorService.isShutdown() || executorService.isTerminated()) {
            return;
        }

        final ListenableFuture<?> response = executorService.submit(new BotReceiveTask(processor, packet));
        Futures.addCallback(response, new FutureCallback<Object>() {
            @Override
            public void onSuccess(Object response) {
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.error("Failure when executing BotReceiveTask. Bot: " + processor + ". packet: " + packet, throwable);
            }
        });
    }

    public void shutdown() {
        ExecutionUtils.shutdown(executorService, 5, TimeUnit.SECONDS);
        Log.info("Shut down complete.");
    }

    private static class BotReceiveTask implements Runnable {

        private final Bot bot;
        private final Packet packet;

        public BotReceiveTask(Bot bot, Packet packet) {
            this.bot = bot;
            this.packet = packet;
        }

        @Override
        public void run() {
            bot.receive(packet);
        }
    }

}
