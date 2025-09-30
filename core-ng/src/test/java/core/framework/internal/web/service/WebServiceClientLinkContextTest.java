package core.framework.internal.web.service;

import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import core.framework.internal.http.HTTPClientImpl;
import core.framework.internal.log.LogManager;
import core.framework.internal.log.Trace;
import core.framework.internal.web.HTTPHandler;
import core.framework.internal.web.bean.RequestBeanWriter;
import core.framework.internal.web.bean.ResponseBeanReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class WebServiceClientLinkContextTest {
    private WebServiceClient webServiceClient;
    private LogManager logManager;

    @BeforeEach
    void createWebServiceClient() {
        logManager = new LogManager();
        var httpClient = new HTTPClientImpl(null, null, Duration.ofSeconds(10), Duration.ofSeconds(20));
        webServiceClient = new WebServiceClient("http://localhost", httpClient, new RequestBeanWriter(), new ResponseBeanReader());
    }

    @Test
    void linkContext() {
        logManager.run("test", null, actionLog -> {
            actionLog.warningContext.maxProcessTimeInNano(Duration.ofSeconds(60).toNanos());

            var request = new HTTPRequest(HTTPMethod.GET, "http://localhost");
            webServiceClient.linkContext(request);
            assertThat(request.headers).containsKeys(HTTPHandler.HEADER_CLIENT.toString(),
                HTTPHandler.HEADER_CORRELATION_ID.toString(),
                HTTPHandler.HEADER_REF_ID.toString());

            assertThat(request.headers)
                .as("should use http client timeout if it is shorter than max process time")
                .containsEntry(HTTPHandler.HEADER_TIMEOUT.toString(), "20000000000");
            return null;
        });
    }

    @Test
    void linkContextWithoutMaxProcessTime() {
        logManager.run("test", null, actionLog -> {
            var request = new HTTPRequest(HTTPMethod.GET, "http://localhost");
            webServiceClient.linkContext(request);
            assertThat(request.headers).containsEntry(HTTPHandler.HEADER_TIMEOUT.toString(), "20000000000");
            return null;
        });
    }

    @Test
    void linkContextWithShortProcessTime() {
        logManager.run("test", null, actionLog -> {
            actionLog.warningContext.maxProcessTimeInNano(Duration.ofSeconds(1).toNanos());

            var request = new HTTPRequest(HTTPMethod.GET, "http://localhost");
            webServiceClient.linkContext(request);
            assertThat(Long.parseLong(request.headers.get(HTTPHandler.HEADER_TIMEOUT.toString())))
                .isGreaterThan(0)
                .isLessThanOrEqualTo(Duration.ofSeconds(1).toNanos());
            return null;
        });
    }

    @Test
    void linkContextWithTrace() {
        logManager.run("test", null, actionLog -> {
            actionLog.trace = Trace.CASCADE;

            var request = new HTTPRequest(HTTPMethod.GET, "http://localhost");
            webServiceClient.linkContext(request);
            assertThat(request.headers.get(HTTPHandler.HEADER_TRACE.toString())).isEqualTo(Trace.CASCADE.name());
            return null;
        });
    }
}
