package app.monitor.job;

import core.framework.http.HTTPClient;
import core.framework.http.HTTPClientException;
import core.framework.http.HTTPResponse;
import core.framework.internal.log.LogManager;
import core.framework.internal.web.api.APIDefinitionResponse;
import core.framework.internal.web.api.APIType;
import core.framework.json.JSON;
import core.framework.kafka.MessagePublisher;
import core.framework.log.message.StatMessage;
import core.framework.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class APIMonitorJobTest {
    @Mock
    HTTPClient httpClient;
    @Mock
    MessagePublisher<StatMessage> publisher;
    private APIMonitorJob job;

    @BeforeEach
    void createAPIMonitorJob() {
        job = new APIMonitorJob(httpClient, List.of("https://website"), publisher);
    }

    @Test
    void checkAPI() {
        var response = new APIDefinitionResponse();
        response.app = "website";
        response.version = "1";
        response.services = List.of();
        response.types = List.of();
        when(httpClient.execute(any())).thenReturn(new HTTPResponse(200, Map.of(), Strings.bytes(JSON.toJSON(response))));

        job.execute(null);
        verifyNoInteractions(publisher);    // first time, not to compare

        job.execute(null);
        verifyNoInteractions(publisher);    // no changes

        var type = new APIType();
        type.type = "bean";
        type.name = "MockType";
        response.types = List.of(type);
        response.version = "2";
        when(httpClient.execute(any())).thenReturn(new HTTPResponse(200, Map.of(), Strings.bytes(JSON.toJSON(response))));

        job.execute(null);      // added new type
        verify(publisher).publish(argThat(message -> "website".equals(message.app)
                                                     && "WARN".equals(message.result)
                                                     && "API_CHANGED".equals(message.errorCode)));
    }

    @Test
    void publishError() {
        when(httpClient.execute(any())).thenThrow(new HTTPClientException("mock", "MOCK_ERROR_CODE", null));
        job.execute(null);
        verify(publisher).publish(argThat(message -> LogManager.APP_NAME.equals(message.app)
                                                     && "ERROR".equals(message.result)
                                                     && "FAILED_TO_COLLECT".equals(message.errorCode)));
    }
}
