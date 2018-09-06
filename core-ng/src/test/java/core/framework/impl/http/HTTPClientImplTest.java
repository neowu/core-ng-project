package core.framework.impl.http;

import core.framework.api.http.HTTPStatus;
import core.framework.http.ContentType;
import core.framework.http.HTTPClientException;
import core.framework.http.HTTPHeaders;
import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        assertThat(new String(body, UTF_8)).isEqualTo("");
    }

    @Test
    void parseHTTPStatus() {
        assertThat(HTTPClientImpl.parseHTTPStatus(200)).isEqualTo(HTTPStatus.OK);
    }

    @Test
    void parseUnsupportedHTTPStatus() {
        assertThatThrownBy(() -> HTTPClientImpl.parseHTTPStatus(525))
                .isInstanceOf(HTTPClientException.class);
    }

    @Test
    void httpRequest() {
        var request = new HTTPRequest(HTTPMethod.POST, "http://localhost/uri");
        request.addParam("query", "value");
        request.accept(ContentType.APPLICATION_JSON);
        request.body("text", ContentType.TEXT_PLAIN);

        HttpUriRequest httpRequest = httpClient.httpRequest(request);
        assertThat(httpRequest.getURI().toString()).isEqualTo("http://localhost/uri?query=value");
        assertThat(httpRequest.getFirstHeader(HTTPHeaders.ACCEPT).getValue()).isEqualTo(ContentType.APPLICATION_JSON.toString());
    }
}
