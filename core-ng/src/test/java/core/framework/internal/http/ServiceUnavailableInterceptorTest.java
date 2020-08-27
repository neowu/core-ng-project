package core.framework.internal.http;

import core.framework.api.http.HTTPStatus;
import okhttp3.Interceptor;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.connection.RealConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class ServiceUnavailableInterceptorTest {
    @Mock
    Interceptor.Chain chain;
    @Mock
    RealConnection connection;
    private ServiceUnavailableInterceptor interceptor;

    @BeforeEach
    void createServiceUnavailableInterceptor() {
        interceptor = new ServiceUnavailableInterceptor();
    }

    @Test
    void interceptWith503() throws IOException {
        var request = new Request.Builder().url("http://localhost").build();
        var response = new Response.Builder().request(request)
                .protocol(Protocol.HTTP_2)
                .code(HTTPStatus.SERVICE_UNAVAILABLE.code)
                .message("service unavailable").build();
        when(chain.request()).thenReturn(request);
        when(chain.proceed(request)).thenReturn(response);
        when(chain.connection()).thenReturn(connection);

        interceptor.intercept(chain);

        verify(connection).setNoNewExchanges(true);
    }

    @Test
    void interceptWithout503() throws IOException {
        var request = new Request.Builder().url("http://localhost").build();
        var response = new Response.Builder().request(request)
                .protocol(Protocol.HTTP_2)
                .code(HTTPStatus.OK.code)
                .message("ok").build();
        when(chain.request()).thenReturn(request);
        when(chain.proceed(request)).thenReturn(response);

        interceptor.intercept(chain);
    }
}
