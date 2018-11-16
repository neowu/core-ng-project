package core.framework.internal.http;

import core.framework.api.http.HTTPStatus;
import core.framework.log.Markers;
import core.framework.util.Threads;
import okhttp3.Interceptor;
import okhttp3.Request;
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
        Request request = chain.request();
        int attempts = 0;
        while (true) {
            attempts++;
            try {
                Response response = chain.proceed(request);
                if (shouldRetry(attempts, response.code())) {
                    logger.warn(Markers.errorCode("HTTP_COMMUNICATION_FAILED"), "service unavailable, retry soon, uri={}", request.url());
                    Threads.sleepRoughly(waitTime(attempts));
                    continue;
                }
                return response;
            } catch (IOException e) {
                if (shouldRetry(attempts, request.method(), e)) {
                    logger.warn(Markers.errorCode("HTTP_COMMUNICATION_FAILED"), "http communication failed, retry soon, uri={}", request.url(), e);
                    Threads.sleepRoughly(waitTime(attempts));
                } else {
                    throw e;
                }
            }
        }
    }

    boolean shouldRetry(int attempts, int statusCode) {
        return attempts < maxRetries && statusCode == HTTPStatus.SERVICE_UNAVAILABLE.code;
    }

    boolean shouldRetry(int attempts, String method, IOException e) {
        if (attempts >= maxRetries) return false;
        /* read timeout exception trace
        Caused by: java.net.SocketTimeoutException: Read timed out
            at java.base/java.net.SocketInputStream.socketRead0(Native Method)
            at java.base/java.net.SocketInputStream.socketRead(SocketInputStream.java:115)
            at java.base/java.net.SocketInputStream.read(SocketInputStream.java:168)
            at java.base/java.net.SocketInputStream.read(SocketInputStream.java:140)
            at okio.Okio$2.read(Okio.java:140)
            at okio.AsyncTimeout$2.read(AsyncTimeout.java:237)
        */
        return !("POST".equals(method) && e.getCause() != null && "Read timed out".equals(e.getCause().getMessage()));
    }

    Duration waitTime(int attempts) {
        return Duration.ofMillis(500 << attempts - 1);
    }
}
