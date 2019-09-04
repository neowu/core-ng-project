package core.framework.internal.http;

import core.framework.api.http.HTTPStatus;
import core.framework.log.Markers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.Duration;

import static okhttp3.internal.Util.closeQuietly;

/**
 * @author neo
 */
public class RetryInterceptor implements Interceptor {
    private final Logger logger = LoggerFactory.getLogger(RetryInterceptor.class);
    private final int maxRetries;
    private final ThreadSleep sleep;

    public RetryInterceptor(int maxRetries, ThreadSleep sleep) {
        this.maxRetries = maxRetries;
        this.sleep = sleep;
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
                    logger.warn(Markers.errorCode("HTTP_REQUEST_FAILED"), "service unavailable, retry soon, uri={}", request.url());
                    closeRequestBody(response);
                    sleep.sleep(waitTime(attempts));
                    continue;
                }
                return response;
            } catch (IOException e) {
                if (shouldRetry(attempts, request.method(), e)) {
                    logger.warn(Markers.errorCode("HTTP_REQUEST_FAILED"), "http request failed, retry soon, uri={}", request.url(), e);
                    sleep.sleep(waitTime(attempts));
                } else {
                    throw e;
                }
            }
        }
    }

    private void closeRequestBody(Response response) {
        ResponseBody responseBody = response.body();
        if (responseBody != null) closeQuietly(responseBody);
    }

    boolean shouldRetry(int attempts, int statusCode) {
        return attempts < maxRetries && statusCode == HTTPStatus.SERVICE_UNAVAILABLE.code;
    }

    boolean shouldRetry(int attempts, String method, IOException e) {
        if (attempts >= maxRetries) return false;
        // not retry on POST with read time out
        // okHTTP uses both socket timeout and AsyncTimeout, asyncTime close socket when timeout is detected by background thread,
        // refer to Okio.kt line: 166
        return !("POST".equals(method) && e instanceof SocketTimeoutException);
    }

    Duration waitTime(int attempts) {
        return Duration.ofMillis(500 << attempts - 1);
    }

    public interface ThreadSleep {
        void sleep(Duration time);
    }
}
