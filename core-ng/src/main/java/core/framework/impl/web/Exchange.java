package core.framework.impl.web;

import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpServerExchange;

/**
 * @author neo
 */
class Exchange { // due to HttpServerExchange is final and can't be mocked (mockito mock final class support has problem with byte buddy/jdk 11/jacoco), this wrapper is mainly for unit test
    private final HttpServerExchange exchange;

    Exchange(HttpServerExchange exchange) {
        this.exchange = exchange;
    }

    void addExchangeCompleteListener(ExchangeCompletionListener listener) {
        exchange.addExchangeCompleteListener(listener);
    }

    String getRequestURL() {
        return exchange.getRequestURL();
    }

    void setStatusCode(int statusCode) {
        exchange.setStatusCode(statusCode);
    }

    void endExchange() {
        exchange.endExchange();
    }
}
