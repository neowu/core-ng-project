package core.framework.internal.http;

import core.framework.api.http.HTTPStatus;
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
            RealConnection connection = (RealConnection) chain.connection();
            if (connection != null) {
                connection.setNoNewExchanges(true);
            }
        }
        return response;
    }
}
