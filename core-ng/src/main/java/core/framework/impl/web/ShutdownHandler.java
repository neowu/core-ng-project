package core.framework.impl.web;

import core.framework.web.exception.ServiceUnavailableException;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author neo
 */
public class ShutdownHandler implements ExchangeCompletionListener {
    final AtomicLong activeRequests = new AtomicLong(0);
    private final Logger logger = LoggerFactory.getLogger(ShutdownHandler.class);
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    private final Object lock = new Object();

    void handleRequest(HttpServerExchange exchange) {
        if (shutdown.get()) {
            throw new ServiceUnavailableException("server is shutting down");
        } else {
            activeRequests.getAndIncrement();
            exchange.addExchangeCompleteListener(this);
        }
    }

    void shutdown() {
        shutdown.set(true);
    }

    void awaitTermination(long timeoutInMs) throws InterruptedException {
        synchronized (lock) {
            long end = System.currentTimeMillis() + timeoutInMs;
            while (activeRequests.get() > 0) {
                long left = end - System.currentTimeMillis();
                if (left <= 0) {
                    logger.warn("failed to wait all http requests to complete");
                    break;
                }
                lock.wait(left);
            }
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
