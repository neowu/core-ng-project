package core.framework.impl.web;

import core.framework.impl.log.ActionLog;
import io.undertow.util.HeaderMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class HTTPServerHandlerTest {
    private HTTPServerHandler handler;

    @BeforeEach
    void createHTTPServerHandler() {
        handler = new HTTPServerHandler(null, null, null);
    }

    @Test
    void linkContext() {
        var actionLog = new ActionLog(null);
        var headers = new HeaderMap();
        headers.put(HTTPServerHandler.HEADER_TRACE, "true");
        handler.linkContext(actionLog, headers);

        assertThat(actionLog.trace).isTrue();
    }
}
