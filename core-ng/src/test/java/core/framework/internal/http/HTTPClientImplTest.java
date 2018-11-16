package core.framework.internal.http;

import core.framework.api.http.HTTPStatus;
import core.framework.http.ContentType;
import core.framework.http.HTTPClientException;
import core.framework.http.HTTPHeaders;
import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import core.framework.http.HTTPResponse;
import core.framework.util.Strings;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
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
        httpClient = new HTTPClientImpl(null, "TestUserAgent", Duration.ofSeconds(10));
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

        Request httpRequest = httpClient.httpRequest(request);
        assertThat(httpRequest.url().toString()).isEqualTo("http://localhost/uri?query=value");
        assertThat(httpRequest.headers().get(HTTPHeaders.ACCEPT)).isEqualTo(ContentType.APPLICATION_JSON.toString());
        assertThat(httpRequest.headers().get(HTTPHeaders.USER_AGENT)).isEqualTo("TestUserAgent");
    }

    @Test
    void httpRequestWithInvalidURL() {
        assertThatThrownBy(() -> httpClient.httpRequest(new HTTPRequest(HTTPMethod.HEAD, "//%%")))
                .isInstanceOf(HTTPClientException.class)
                .hasMessageContaining("uri is invalid");
    }

    @Test
    void response() throws IOException {
        Response httpResponse = new Response.Builder().request(new Request.Builder().url("http://localhost/uri").build())
                                                      .protocol(Protocol.HTTP_1_1)
                                                      .code(200)
                                                      .message("OK")
                                                      .header("content-type", "text/html")
                                                      .body(ResponseBody.create(MediaType.get("application/json"), Strings.bytes("{}")))
                                                      .build();

        HTTPResponse response = httpClient.response(httpResponse);
        assertThat(response.status).isEqualTo(HTTPStatus.OK);
        assertThat(response.contentType.mediaType).isEqualTo(ContentType.TEXT_HTML.mediaType);
        assertThat(response.headers).containsEntry(HTTPHeaders.CONTENT_TYPE, "text/html");
        assertThat(response.text()).isEqualTo("{}");
    }
}
