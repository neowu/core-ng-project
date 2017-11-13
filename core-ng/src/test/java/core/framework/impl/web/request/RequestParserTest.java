package core.framework.impl.web.request;

import core.framework.web.exception.MethodNotAllowedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author neo
 */
class RequestParserTest {
    private RequestParser parser;

    @BeforeEach
    void createRequestParser() {
        parser = new RequestParser();
    }

    @Test
    void port() {
        assertEquals(80, parser.port(80, null));
        assertEquals(443, parser.port(80, "443"));
        assertEquals(443, parser.port(80, "443, 80"));
    }

    @Test
    void requestPort() {
        assertEquals(443, parser.requestPort("127.0.0.1", "https", null));
        assertEquals(8080, parser.requestPort("127.0.0.1:8080", "http", null));
    }

    @Test
    void httpMethod() {
        MethodNotAllowedException exception = assertThrows(MethodNotAllowedException.class, () -> parser.httpMethod("TRACK"));
        assertThat(exception.getMessage(), containsString("method=TRACK"));
    }
}
