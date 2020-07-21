package core.framework.internal.http;

import core.framework.api.http.HTTPStatus;
import okhttp3.Connection;
import okhttp3.Interceptor;
import okhttp3.Response;
import okhttp3.internal.connection.RealConnection;

import java.io.IOException;

/**
 * @author neo
 */
public class ServiceUnavailableInterceptor implements Interceptor {
    // only network interceptor is able to access connection,
    // this is to obsolete the current connection if got 503, during remote graceful shutdown process (kube/service)
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        if (response.code() == HTTPStatus.SERVICE_UNAVAILABLE.code) {
            ConnectionWrapper connection = connection(chain);
            connection.noNewExchanges();
        }
        return response;
    }

    ConnectionWrapper connection(Chain chain) {
        // refer to Interceptor.connection(), only application interceptor returns null, here we always use as network interceptor
        return new ConnectionWrapper(chain.connection());
    }

    // for unit test, RealConnection can not be mocked
    static class ConnectionWrapper {
        final Connection connection;

        ConnectionWrapper(Connection connection) {
            this.connection = connection;
        }

        void noNewExchanges() {
            ((RealConnection) connection).setNoNewExchanges(true);
        }
    }
}
