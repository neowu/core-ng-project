package core.framework.impl.http;

import core.framework.api.http.HTTPStatus;
import core.framework.http.ContentType;
import core.framework.http.HTTPClientException;
import core.framework.http.HTTPHeaders;
import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import core.framework.util.Charsets;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author neo
 */
class HTTPClientImplTest {
    private HTTPClientImpl httpClient;

    @BeforeEach
    void createHTTPClient() {
        httpClient = new HTTPClientImpl(null, "", Duration.ZERO);
    }

    @Test
    void responseBodyWithNoContent() throws IOException {
        byte[] body = httpClient.responseBody(null);   // apache http client return null for HEAD/204/205/304

        assertEquals("", new String(body, Charsets.UTF_8));
    }

    @Test
    void parseHTTPStatus() {
        assertEquals(HTTPStatus.OK, HTTPClientImpl.parseHTTPStatus(200));
    }

    @Test
    void parseUnsupportedHTTPStatus() {
        assertThrows(HTTPClientException.class, () -> HTTPClientImpl.parseHTTPStatus(525));
    }

    @Test
    void httpRequest() {
        HTTPRequest request = new HTTPRequest(HTTPMethod.POST, "http://localhost/uri");
        request.addParam("query", "value");
        request.accept(ContentType.APPLICATION_JSON);
        request.body("text", ContentType.TEXT_PLAIN);

        HttpUriRequest httpRequest = httpClient.httpRequest(request);
        assertEquals("http://localhost/uri?query=value", httpRequest.getURI().toString());
        assertEquals(ContentType.APPLICATION_JSON.toString(), httpRequest.getFirstHeader(HTTPHeaders.ACCEPT).getValue());
    }
}
