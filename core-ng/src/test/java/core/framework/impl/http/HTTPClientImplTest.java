package core.framework.impl.http;

import core.framework.api.http.HTTPStatus;
import core.framework.http.ContentType;
import core.framework.http.HTTPClientException;
import core.framework.http.HTTPHeaders;
import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpRequest;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class HTTPClientImplTest {
    private HTTPClientImpl httpClient;

    @BeforeEach
    void createHTTPClient() {
        httpClient = new HTTPClientImpl(null, "", Duration.ofSeconds(10), Duration.ZERO);
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
        request.param("query", "value");
        request.accept(ContentType.APPLICATION_JSON);
        request.body("text", ContentType.TEXT_PLAIN);

        HttpRequest httpRequest = httpClient.httpRequest(request);
        assertThat(httpRequest.uri().toString()).isEqualTo("http://localhost/uri?query=value");
        assertThat(httpRequest.headers().firstValue(HTTPHeaders.ACCEPT)).get().isEqualTo(ContentType.APPLICATION_JSON.toString());
    }
}
