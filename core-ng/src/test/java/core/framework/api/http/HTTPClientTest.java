package core.framework.api.http;

import core.framework.api.util.Charsets;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.time.Duration;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class HTTPClientTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    HTTPClient httpClient;

    @Before
    public void createHTTPClient() {
        httpClient = new HTTPClient(null, Duration.ofSeconds(5));
    }

    @Test
    public void responseBodyWithNoContent() throws IOException {
        byte[] body = httpClient.responseBody(null);   // apache http client return null for HEAD/204/205/304

        assertEquals("", new String(body, Charsets.UTF_8));
    }

    @Test
    public void parseHTTPStatus() {
        assertEquals(HTTPStatus.OK, HTTPClient.parseHTTPStatus(200));
    }

    @Test
    public void parseUnsupportedHTTPStatus() {
        exception.expect(HTTPClientException.class);

        HTTPClient.parseHTTPStatus(525);
    }
}