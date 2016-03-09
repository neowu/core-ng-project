package core.framework.impl.inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author neo
 */
public final class ShutdownHook implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(ShutdownHook.class);

    private final Deque<Runnable> methods = new ArrayDeque<>();

    public ShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this, "shutdown"));
    }

    public void add(Runnable method) {
        methods.addFirst(method);
    }

    @Override
    public void run() {
        for (Runnable method : methods) {
            try {
                method.run();
            } catch (Throwable e) {
                logger.warn("failed to execute shutdown method, method={}", method, e);
            }
        }
    }
}
