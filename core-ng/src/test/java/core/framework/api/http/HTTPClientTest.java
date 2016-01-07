package core.framework.api.http;

import core.framework.api.util.ByteBuf;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class HTTPClientTest {
    HTTPClient httpClient;

    @Before
    public void createHTTPClient() {
        httpClient = new HTTPClient(null, 0);
    }

    @Test
    public void responseBodyWithNoContent() throws IOException {
        ByteBuf body = httpClient.responseBody(null);   // apache http client return null for HEAD/204/205/304

        assertEquals("", body.text());
    }
}