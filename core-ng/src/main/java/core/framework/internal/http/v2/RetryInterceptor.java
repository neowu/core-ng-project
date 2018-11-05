package core.framework.internal.http.v2;

import core.framework.api.http.HTTPStatus;
import core.framework.log.Markers;
import core.framework.util.Threads;
import okhttp3.Interceptor;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;

/**
 * @author neo
 */
public class RetryInterceptor implements Interceptor {
    private final Logger logger = LoggerFactory.getLogger(RetryInterceptor.class);
    private final int maxRetries;

    public RetryInterceptor(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        int attempts = 0;
        while (true) {
            attempts++;
            Response response = chain.proceed(chain.request());
            if (attempts >= maxRetries || response.code() != HTTPStatus.SERVICE_UNAVAILABLE.code) {
                return response;
            }
            logger.warn(Markers.errorCode("HTTP_COMMUNICATION_FAILED"), "service unavailable, retry soon, uri={}", chain.request().url());
            Threads.sleepRoughly(waitTime(attempts));
        }
    }

    Duration waitTime(int attempts) {
        return Duration.ofMillis(500 << attempts - 1);
    }
}
