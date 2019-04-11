package core.framework.internal.http;

import core.framework.api.http.HTTPStatus;
import okhttp3.Interceptor;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.connection.RealConnection;
import okhttp3.internal.connection.RealConnectionPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class ServiceUnavailableInterceptorTest {
    private ServiceUnavailableInterceptor interceptor;

    @BeforeEach
    void createServiceUnavailableInterceptor() {
        interceptor = new ServiceUnavailableInterceptor();
    }

    @Test
    void intercept() throws IOException, NoSuchFieldException, IllegalAccessException {
        var connection = new RealConnection(new RealConnectionPool(0, 10, TimeUnit.SECONDS), null);
        var request = new Request.Builder().url("http://localhost").build();
        var response = new Response.Builder().request(request)
                                             .protocol(Protocol.HTTP_2)
                                             .code(HTTPStatus.SERVICE_UNAVAILABLE.code)
                                             .message("service unavailable").build();
        Interceptor.Chain chain = mock(Interceptor.Chain.class);
        when(chain.proceed(any())).thenReturn(response);
        when(chain.connection()).thenReturn(connection);

        interceptor.intercept(chain);
        Field field = RealConnection.class.getDeclaredField("noNewExchanges");

        if (!field.trySetAccessible())
            throw new Error("failed to access noNewExchanges field");
        assertThat((boolean) field.get(connection)).isTrue();
    }
}
