package core.framework.impl.web.request;

import core.framework.http.ContentType;
import core.framework.util.Strings;
import core.framework.web.exception.MethodNotAllowedException;
import io.undertow.server.HttpServerExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
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
        assertThat(exception.getMessage()).contains("method=TRACK");
    }

    @Test
    void parseQueryParams() throws UnsupportedEncodingException {
        RequestImpl request = new RequestImpl(null, null);
        Map<String, Deque<String>> params = new HashMap<>();
        params.put("key", new ArrayDeque<>());
        params.get("key").add(URLEncoder.encode("value1 value2", "UTF-8"));     // undertow url decoding is disabled in core.framework.impl.web.HTTPServer.start, so the parser must decode all query param
        parser.parseQueryParams(request, params);

        assertEquals("value1 value2", request.queryParam("key").orElse(null));
    }

    @Test
    void parseBody() throws Throwable {
        RequestImpl request = new RequestImpl(null, null);
        request.contentType = ContentType.TEXT_XML;
        HttpServerExchange exchange = new HttpServerExchange(null, -1);
        byte[] body = Strings.bytes("<xml/>");
        exchange.putAttachment(RequestBodyReader.REQUEST_BODY, new RequestBodyReader.RequestBody(body, null));
        parser.parseBody(request, exchange);

        assertThat(request.body()).hasValue(body);
    }
}
