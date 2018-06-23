package core.framework.impl.module;

import core.framework.impl.async.ExecutorImpl;
import core.framework.impl.kafka.KafkaMessageListener;
import core.framework.impl.scheduler.Scheduler;
import core.framework.impl.web.HTTPServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author neo
 */
public final class ShutdownHook implements Runnable {
    public final List<KafkaMessageListener> kafkaListeners = new ArrayList<>();
    public final List<ExecutorImpl> executors = new ArrayList<>();
    public final List<Runnable> methods = new ArrayList<>();
    private final Logger logger = LoggerFactory.getLogger(ShutdownHook.class);
    public Scheduler scheduler;
    BackgroundTaskExecutor backgroundTask;
    HTTPServer httpServer;

    ShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this, "shutdown"));
    }

    @Override
    public void run() {
        shutdownGracefully();

        for (Runnable method : methods) {
            try {
                method.run();
            } catch (Throwable e) {
                logger.warn("failed to execute shutdown method, method={}", method, e);
            }
        }
    }

    private void shutdownGracefully() {
        long endTime = System.currentTimeMillis() + 25000;  // default kube terminationGracePeriodSeconds is 30s, here give 25s try to stop important processes

        // start shutdown process
        if (httpServer != null) httpServer.shutdown();
        if (scheduler != null) scheduler.shutdown();
        kafkaListeners.forEach(KafkaMessageListener::shutdown);
        if (backgroundTask != null) backgroundTask.shutdown();

        // await to finish current processes
        if (httpServer != null) httpServer.awaitTermination(endTime - System.currentTimeMillis());
        if (scheduler != null) scheduler.awaitTermination(endTime - System.currentTimeMillis());
        kafkaListeners.forEach(listener -> listener.awaitTermination(endTime - System.currentTimeMillis()));

        // stop executors after no more new requests
        executors.forEach(executor -> executor.stop(endTime - System.currentTimeMillis()));
        if (backgroundTask != null) backgroundTask.awaitTermination(endTime - System.currentTimeMillis());
    }
}
