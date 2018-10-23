package core.framework.impl.web;

import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author neo
 */
public class ShutdownHandler implements ExchangeCompletionListener {
    public final AtomicBoolean shutdown = new AtomicBoolean(false);
    final AtomicLong activeRequests = new AtomicLong(0);
    private final Logger logger = LoggerFactory.getLogger(ShutdownHandler.class);
    private final Object lock = new Object();

    boolean handle(Exchange exchange) {
        activeRequests.getAndIncrement();
        exchange.addExchangeCompleteListener(this);

        if (shutdown.get()) {
            logger.warn("reject request due to server is shutting down, requestURL={}", exchange.getRequestURL());
            exchange.setPersistent(false);  // ask client not set keep alive for current connection, with persistent=false, undertow will send header "connection: close"
            exchange.setStatusCode(StatusCodes.SERVICE_UNAVAILABLE);
            exchange.endExchange();
            return true;
        }

        return false;
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
            if (count <= 0 && shutdown.get()) {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        } finally {
            next.proceed();
        }
    }
}
