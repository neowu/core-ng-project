package core.framework.internal.web;

import core.framework.internal.stat.Counter;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author neo
 */
public class ShutdownHandler implements ExchangeCompletionListener {
    final Counter activeRequests = new Counter();

    private final Logger logger = LoggerFactory.getLogger(ShutdownHandler.class);

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition noMoreActiveRequest = lock.newCondition();

    private volatile boolean shutdown;

    boolean handle(HttpServerExchange exchange) {
        activeRequests.increase();
        exchange.addExchangeCompleteListener(this);

        if (shutdown) {
            logger.warn("reject request due to server is shutting down, requestURL={}", exchange.getRequestURL());
            // ask client not set keep alive for current connection, with persistent=false, undertow will send header "connection: close",
            // this does no effect with http/2.0, only for http/1.1
            // refer to io.undertow.server.protocol.http.HttpTransferEncoding.createSinkConduit
            exchange.setPersistent(false);
            exchange.setStatusCode(StatusCodes.SERVICE_UNAVAILABLE);
            exchange.endExchange();
            return true;
        }

        return false;
    }

    void shutdown() {
        shutdown = true;
    }

    boolean awaitTermination(long timeoutInMs) throws InterruptedException {
        long end = System.currentTimeMillis() + timeoutInMs;
        lock.lock();
        try {
            while (activeRequests.get() > 0) {
                long left = end - System.currentTimeMillis();
                if (left <= 0) {
                    return false;
                }
                noMoreActiveRequest.await(left, TimeUnit.MILLISECONDS);
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void exchangeEvent(HttpServerExchange exchange, NextListener next) {
        try {
            int count = activeRequests.decrease();
            if (count <= 0 && shutdown) {
                signal();
            }
        } finally {
            next.proceed();
        }
    }

    private void signal() {
        lock.lock();
        try {
            noMoreActiveRequest.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
