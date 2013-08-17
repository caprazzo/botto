package botto.xmpp.service.component;

import com.google.common.util.concurrent.*;
import botto.xmpp.service.Bot;
import net.caprazzi.reusables.threading.ExecutorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.Packet;

import java.util.concurrent.Callable;
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

        final ListenableFuture<Packet> response = executorService.submit(new BotReceiveTask(processor, packet));
        Futures.addCallback(response, new FutureCallback<Packet>() {
            @Override
            public void onSuccess(Packet response) {
                // TODO: send response to output
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.error("Failure when executing BotReceiveTask. Bot: " + processor + ". packet: " + packet, throwable);
            }
        });
    }

    public void stop() {
        ExecutorUtils.shutdown(Log, executorService, 5, TimeUnit.SECONDS);
    }

    private static class BotReceiveTask implements Callable<Packet> {

        private final Bot bot;
        private final Packet packet;

        public BotReceiveTask(Bot bot, Packet packet) {
            this.bot = bot;
            this.packet = packet;
        }

        @Override
        public Packet call() {
            return bot.receive(packet);
        }
    }

}
