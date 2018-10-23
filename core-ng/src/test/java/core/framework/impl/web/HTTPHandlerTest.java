package core.framework.impl.web;

import core.framework.impl.log.ActionLog;
import io.undertow.util.HeaderMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class HTTPHandlerTest {
    private HTTPHandler handler;

    @BeforeEach
    void createHTTPServerHandler() {
        handler = new HTTPHandler(null, null, null, null);
    }

    @Test
    void linkContext() {
        var actionLog = new ActionLog(null);
        var headers = new HeaderMap();
        headers.put(HTTPHandler.HEADER_TRACE, "true");
        headers.put(HTTPHandler.HEADER_CLIENT, "client");
        handler.linkContext(actionLog, headers);

        assertThat(actionLog.trace).isTrue();
        assertThat(actionLog.clients).containsExactly("client");
    }
}
