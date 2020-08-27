package core.framework.internal.http;

import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import okhttp3.Interceptor;
import okhttp3.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.when;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class TimeoutInterceptorTest {
    @Mock
    Interceptor.Chain chain;
    private TimeoutInterceptor interceptor;

    @BeforeEach
    void createTimeoutInterceptor() {
        interceptor = new TimeoutInterceptor();
    }

    @Test
    void interceptWithConnectTimeout() throws IOException {
        var httpRequest = new HTTPRequest(HTTPMethod.POST, "https://localhost");
        httpRequest.connectTimeout = Duration.ofSeconds(30);
        var request = new Request.Builder().url(httpRequest.uri).tag(HTTPRequest.class, httpRequest).build();
        when(chain.request()).thenReturn(request);

        when(chain.withConnectTimeout(30000, TimeUnit.MILLISECONDS)).thenReturn(chain);

        interceptor.intercept(chain);
    }

    @Test
    void interceptWithTimeout() throws IOException {
        var httpRequest = new HTTPRequest(HTTPMethod.POST, "https://localhost");
        httpRequest.timeout = Duration.ofSeconds(30);
        var request = new Request.Builder().url(httpRequest.uri).tag(HTTPRequest.class, httpRequest).build();
        when(chain.request()).thenReturn(request);

        when(chain.withReadTimeout(30000, TimeUnit.MILLISECONDS)).thenReturn(chain);
        when(chain.withWriteTimeout(30000, TimeUnit.MILLISECONDS)).thenReturn(chain);

        interceptor.intercept(chain);
    }

    @Test
    void interceptWithoutTimeout() throws IOException {
        var request = new Request.Builder().url("http://localhost").build();
        when(chain.request()).thenReturn(request);

        interceptor.intercept(chain);
    }
}
