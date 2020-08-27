package core.framework.internal.http;

import core.framework.http.HTTPRequest;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author neo
 */
public class TimeoutInterceptor implements Interceptor {
    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Interceptor.Chain chainWithTimeout = chain;
        Request request = chain.request();
        HTTPRequest httpRequest = request.tag(HTTPRequest.class);
        if (httpRequest != null) {
            if (httpRequest.connectTimeout != null) {
                chainWithTimeout = chainWithTimeout.withConnectTimeout((int) httpRequest.connectTimeout.toMillis(), TimeUnit.MILLISECONDS);
            }
            if (httpRequest.timeout != null) {
                chainWithTimeout = chainWithTimeout.withReadTimeout((int) httpRequest.timeout.toMillis(), TimeUnit.MILLISECONDS)
                        .withWriteTimeout((int) httpRequest.timeout.toMillis(), TimeUnit.MILLISECONDS);
            }
        }
        return chainWithTimeout.proceed(request);
    }
}
