package core.framework.impl.web;

import core.framework.web.exception.ServiceUnavailableException;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpServerExchange;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author neo
 */
public class ShutdownHandler implements ExchangeCompletionListener {
    final AtomicLong activeRequests = new AtomicLong(0);
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    private final Object lock = new Object();

    void handleRequest(HttpServerExchange exchange) {
        activeRequests.getAndIncrement();
        exchange.addExchangeCompleteListener(this);

        if (shutdown.get())
            throw new ServiceUnavailableException("server is shutting down");
    }

    void shutdown() {
        shutdown.set(true);
    }

    boolean awaitTermination(long timeoutInMs) throws InterruptedException {
        long end = System.currentTimeMillis() + timeoutInMs;
        synchronized (lock) {
            while (activeRequests.get() > 0) {
                long left = end - System.currentTimeMillis();
                if (left <= 0) {
                    return false;
                }
                lock.wait(left);
            }
            return true;
        }
    }

    @Override
    public void exchangeEvent(HttpServerExchange exchange, NextListener next) {
        try {
            long count = activeRequests.decrementAndGet();
            if (shutdown.get() && count <= 0) {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        } finally {
            next.proceed();
        }
    }
}
