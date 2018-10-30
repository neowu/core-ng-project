package core.framework.internal.http;

import core.framework.api.http.HTTPStatus;
import core.framework.http.ContentType;
import core.framework.http.HTTPClientException;
import core.framework.http.HTTPHeaders;
import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import core.framework.http.HTTPResponse;
import core.framework.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class HTTPClientImplTest {
    private HTTPClientImpl httpClient;

    @BeforeEach
    void createHTTPClient() {
        httpClient = new HTTPClientImpl(null, "TestUserAgent", Duration.ofSeconds(10), 3, Duration.ZERO);
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
        request.params.put("query", "value");
        request.accept(ContentType.APPLICATION_JSON);
        request.body("text", ContentType.TEXT_PLAIN);

        HttpRequest httpRequest = httpClient.httpRequest(request);
        assertThat(httpRequest.uri().toString()).isEqualTo("http://localhost/uri?query=value");
        assertThat(httpRequest.headers().firstValue(HTTPHeaders.ACCEPT)).get().isEqualTo(ContentType.APPLICATION_JSON.toString());
        assertThat(httpRequest.headers().firstValue(HTTPHeaders.USER_AGENT)).get().isEqualTo("TestUserAgent");
        assertThat(httpRequest.version()).isEmpty();
    }

    @Test
    void httpRequestWithInvalidURL() {
        assertThatThrownBy(() -> httpClient.httpRequest(new HTTPRequest(HTTPMethod.HEAD, "//%%")))
                .isInstanceOf(HTTPClientException.class)
                .hasMessageContaining("uri is invalid");
    }

    @Test
    void response() {
        @SuppressWarnings("unchecked")
        HttpResponse<byte[]> httpResponse = mock(HttpResponse.class);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.headers()).thenReturn(HttpHeaders.of(Map.of("content-type", List.of("text/html"), ":status", List.of("200")), (name, value) -> true));

        HTTPResponse response = httpClient.response(httpResponse);
        assertThat(response.status).isEqualTo(HTTPStatus.OK);
        assertThat(response.contentType.mediaType).isEqualTo(ContentType.TEXT_HTML.mediaType);
        assertThat(response.headers)
                .doesNotContainKeys(":status")
                .containsEntry(HTTPHeaders.CONTENT_TYPE, "text/html");
    }

    @Test
    void decodeGZipBody() throws IOException {
        var stream = new ByteArrayOutputStream();
        try (var gzip = new GZIPOutputStream(stream)) {
            gzip.write(Strings.bytes("<html/>"));
        }
        byte[] body = httpClient.decodeBody("gzip", stream.toByteArray());
        assertThat(new String(body, UTF_8)).isEqualTo("<html/>");
    }

    @Test
    void decodeBody() {
        byte[] bytes = Strings.bytes("<html/>");
        assertThat(httpClient.decodeBody(null, bytes)).isSameAs(bytes);
        assertThat(httpClient.decodeBody("identity", bytes)).isSameAs(bytes);
    }

    @Test
    void waitTime() {
        assertThat(httpClient.waitTime(1)).isEqualTo(Duration.ofMillis(500));
        assertThat(httpClient.waitTime(2)).isEqualTo(Duration.ofSeconds(1));
        assertThat(httpClient.waitTime(3)).isEqualTo(Duration.ofSeconds(2));
        assertThat(httpClient.waitTime(4)).isEqualTo(Duration.ofSeconds(4));
    }

    @Test
    void shouldRetry() {
        assertThat(httpClient.shouldRetry(1, HTTPMethod.GET, null, HTTPStatus.OK)).isFalse();

        assertThat(httpClient.shouldRetry(1, HTTPMethod.POST, null, HTTPStatus.CREATED)).isFalse();
        assertThat(httpClient.shouldRetry(2, HTTPMethod.POST, null, HTTPStatus.CREATED)).isFalse();
    }

    @Test
    void shouldRetryWithConnectionException() {
        assertThat(httpClient.shouldRetry(1, HTTPMethod.GET, new HttpTimeoutException("read timeout"), null)).isTrue();
        assertThat(httpClient.shouldRetry(2, HTTPMethod.GET, new ConnectException("connection failed"), null)).isTrue();
        assertThat(httpClient.shouldRetry(3, HTTPMethod.GET, new ConnectException("connection failed"), null)).isFalse();

        assertThat(httpClient.shouldRetry(1, HTTPMethod.POST, new HttpConnectTimeoutException("connection timeout"), null)).isTrue();
        assertThat(httpClient.shouldRetry(1, HTTPMethod.POST, new ConnectException("connection refused"), null)).isTrue();
        assertThat(httpClient.shouldRetry(1, HTTPMethod.POST, new HttpTimeoutException("read timeout"), null)).isFalse();

        assertThat(httpClient.shouldRetry(2, HTTPMethod.PUT, new HttpTimeoutException("read timeout"), null)).isTrue();
    }

    @Test
    void shouldRetryWithServiceUnavailable() {
        assertThat(httpClient.shouldRetry(1, HTTPMethod.POST, null, HTTPStatus.SERVICE_UNAVAILABLE)).isTrue();
        assertThat(httpClient.shouldRetry(3, HTTPMethod.POST, null, HTTPStatus.SERVICE_UNAVAILABLE)).isFalse();

        assertThat(httpClient.shouldRetry(1, HTTPMethod.PUT, null, HTTPStatus.SERVICE_UNAVAILABLE)).isTrue();
        assertThat(httpClient.shouldRetry(3, HTTPMethod.PUT, null, HTTPStatus.SERVICE_UNAVAILABLE)).isFalse();
    }
}
