package core.framework.internal.web;

import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author neo
 */
public class ShutdownHandler implements ExchangeCompletionListener {
    final AtomicInteger activeRequests = new AtomicInteger(0);

    private final Logger logger = LoggerFactory.getLogger(ShutdownHandler.class);
    private final Object lock = new Object();
    private final AtomicInteger maxActiveRequests = new AtomicInteger(0);
    private volatile boolean shutdown;

    boolean handle(HttpServerExchange exchange) {
        int current = activeRequests.incrementAndGet();
        maxActiveRequests.getAndAccumulate(current, Math::max);     // only increase active request triggers max active request process, doesn't need to handle when active requests decrease
        exchange.addExchangeCompleteListener(this);

        if (shutdown) {
            logger.warn("reject request due to server is shutting down, requestURL={}", exchange.getRequestURL());
            // ask client not set keep alive for current connection, with persistent=false, undertow will send header "connection: close",
            // this does no effect with http/2.0, only for http/1.1
            exchange.setPersistent(false);
            exchange.setStatusCode(StatusCodes.SERVICE_UNAVAILABLE);
            exchange.endExchange();
            return true;
        }

        return false;
    }

    // return max/peak active requests since last call, and reset max with current value
    int maxActiveRequests() {
        return maxActiveRequests.getAndSet(activeRequests.get());
    }

    void shutdown() {
        shutdown = true;
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
            int count = activeRequests.decrementAndGet();
            if (count <= 0 && shutdown) {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        } finally {
            next.proceed();
        }
    }
}
