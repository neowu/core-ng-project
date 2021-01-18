package core.framework.internal.http;

import core.framework.http.ContentType;
import core.framework.http.HTTPClientException;
import core.framework.http.HTTPHeaders;
import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import core.framework.http.HTTPResponse;
import core.framework.util.Strings;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class HTTPClientImplTest {
    @Mock
    OkHttpClient okHttpClient;
    private HTTPClientImpl httpClient;

    @BeforeEach
    void createHTTPClient() {
        httpClient = new HTTPClientImpl(okHttpClient, "TestUserAgent", Duration.ofSeconds(10), Duration.ofSeconds(30));
    }

    @Test
    void httpRequest() {
        var request = new HTTPRequest(HTTPMethod.POST, "http://localhost/uri");
        request.params.put("query", "value");
        request.accept(ContentType.APPLICATION_JSON);
        request.body("text", ContentType.TEXT_PLAIN);
        request.connectTimeout = Duration.ofSeconds(30);

        Request httpRequest = httpClient.httpRequest(request);
        assertThat(httpRequest.url().toString()).isEqualTo("http://localhost/uri?query=value");
        assertThat(httpRequest.headers().get(HTTPHeaders.ACCEPT)).isEqualTo(ContentType.APPLICATION_JSON.toString());
        assertThat(httpRequest.headers().get(HTTPHeaders.USER_AGENT)).isEqualTo("TestUserAgent");
        assertThat(httpRequest.tag(HTTPRequest.class)).isSameAs(request);
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
                .protocol(Protocol.HTTP_1_1).code(200).message("")
                .header("content-type", "text/html")
                .body(ResponseBody.create(Strings.bytes("<html/>"), MediaType.get("text/html")))
                .build();

        HTTPResponse response = httpClient.response(httpResponse);
        assertThat(response.statusCode).isEqualTo(200);
        assertThat(response.contentType.mediaType).isEqualTo(ContentType.TEXT_HTML.mediaType);
        assertThat(response.headers).containsEntry(HTTPHeaders.CONTENT_TYPE, "text/html");
        assertThat(response.text()).isEqualTo("<html/>");
    }

    @Test
    void responseWith204() throws IOException {
        Response httpResponse = new Response.Builder().request(new Request.Builder().url("http://localhost/uri").build())
                .protocol(Protocol.HTTP_2).code(204).message("")
                .build();

        HTTPResponse response = httpClient.response(httpResponse);
        assertThat(response.statusCode).isEqualTo(204);
        assertThat(response.text()).isEmpty();
    }

    @Test
    void mediaType() {
        assertThat(httpClient.mediaType(null)).isNull();

        assertThat(httpClient.mediaType(ContentType.APPLICATION_JSON))
                .isSameAs(httpClient.mediaType(ContentType.APPLICATION_JSON));

        assertThat(httpClient.mediaType(ContentType.APPLICATION_FORM_URLENCODED).toString())
                .isEqualTo(ContentType.APPLICATION_FORM_URLENCODED.mediaType);
    }

    @Test
    void slowOperationThresholdInNanos() {
        var request = new HTTPRequest(HTTPMethod.POST, "http://localhost/uri");
        assertThat(httpClient.slowOperationThresholdInNanos(request)).isEqualTo(Duration.ofSeconds(10).toNanos());

        request.slowOperationThreshold = Duration.ofSeconds(1);
        assertThat(httpClient.slowOperationThresholdInNanos(request)).isEqualTo(Duration.ofSeconds(1).toNanos());
    }

    @Test
    void execute() throws IOException {
        Response httpResponse = new Response.Builder().request(new Request.Builder().url("http://localhost/uri").build())
                .protocol(Protocol.HTTP_1_1).code(200).message("OK")
                .header("content-type", "text/html")
                .body(ResponseBody.create(Strings.bytes("<html/>"), MediaType.get("text/html")))
                .build();
        Call call = mock(Call.class);
        when(okHttpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(httpResponse);

        HTTPResponse response = httpClient.execute(new HTTPRequest(HTTPMethod.GET, "http://localhost/uri"));
        assertThat(response.statusCode).isEqualTo(200);
    }
}
