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
        handler = new HTTPServerHandler(null, null, null, null);
    }

    @Test
    void linkContext() {
        ActionLog actionLog = new ActionLog(null, null);
        HeaderMap headers = new HeaderMap();
        headers.put(HTTPServerHandler.HEADER_TRACE, "true");
        handler.linkContext(actionLog, headers);

        assertThat(actionLog.trace).isTrue();
    }
}
