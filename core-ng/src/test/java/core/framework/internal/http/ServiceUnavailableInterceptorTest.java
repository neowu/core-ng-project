package core.framework.internal.http;

import core.framework.api.http.HTTPStatus;
import okhttp3.Interceptor;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class ServiceUnavailableInterceptorTest {
    private ServiceUnavailableInterceptor interceptor;
    private ServiceUnavailableInterceptor.ConnectionWrapper connection;

    @BeforeEach
    void createServiceUnavailableInterceptor() {
        connection = mock(ServiceUnavailableInterceptor.ConnectionWrapper.class);
        interceptor = new ServiceUnavailableInterceptor() {
            @Override
            ConnectionWrapper connection(Chain chain) {
                return connection;
            }
        };
    }

    @Test
    void intercept() throws IOException {
        var request = new Request.Builder().url("http://localhost").build();
        var response = new Response.Builder().request(request)
                                             .protocol(Protocol.HTTP_2)
                                             .code(HTTPStatus.SERVICE_UNAVAILABLE.code)
                                             .message("service unavailable").build();
        Interceptor.Chain chain = mock(Interceptor.Chain.class);
        when(chain.proceed(any())).thenReturn(response);

        interceptor.intercept(chain);

        verify(connection).noNewExchanges();
    }
}
