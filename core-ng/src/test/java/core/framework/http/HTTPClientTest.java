package core.framework.http;

import core.framework.api.http.HTTPStatus;
import core.framework.util.Charsets;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author neo
 */
class HTTPClientTest {
    @Test
    void responseBodyWithNoContent() throws IOException {
        byte[] body = HTTPClient.responseBody(null);   // apache http client return null for HEAD/204/205/304

        assertEquals("", new String(body, Charsets.UTF_8));
    }

    @Test
    void parseHTTPStatus() {
        assertEquals(HTTPStatus.OK, HTTPClient.parseHTTPStatus(200));
    }

    @Test
    void parseUnsupportedHTTPStatus() {
        assertThrows(HTTPClientException.class, () -> HTTPClient.parseHTTPStatus(525));
    }
}
