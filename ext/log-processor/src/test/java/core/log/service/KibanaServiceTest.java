package core.log.service;

import core.framework.http.HTTPClient;
import core.framework.http.HTTPClientException;
import core.framework.http.HTTPRequest;
import core.framework.http.HTTPResponse;
import core.framework.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class KibanaServiceTest {
    @Mock
    HTTPClient httpClient;
    private KibanaService service;

    @BeforeEach
    void createKibanaService() {
        service = new KibanaService("http://kibana:5601", null, "banner", httpClient);
    }

    @Test
    void importObjects() {
        when(httpClient.execute(argThat(request -> {
            assertThat(request.headers).containsEntry("kbn-xsrf", "true");
            assertThat(new String(request.body, StandardCharsets.UTF_8)).doesNotContain("${NOTIFICATION_BANNER}");
            return true;
        }))).thenReturn(new HTTPResponse(200, Map.of(), Strings.bytes("acknowledged")));

        service.importObjects();
    }

    @Test
    void failedToConnectToKibana() {
        when(httpClient.execute(any(HTTPRequest.class))).thenThrow(new HTTPClientException("failed to connect", "HTTP_REQUEST_FAILED", null));

        service.importObjects();
    }

    @Test
    void failedToImportObjects() {
        when(httpClient.execute(any(HTTPRequest.class)))
            .thenReturn(new HTTPResponse(400, Map.of(), new byte[0]));

        service.importObjects();
    }
}
