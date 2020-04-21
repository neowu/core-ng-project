package core.framework.internal.http;

import core.framework.api.http.HTTPStatus;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.Duration;

import static core.framework.log.Markers.errorCode;
import static okhttp3.internal.Util.closeQuietly;

/**
 * @author neo
 */
public class RetryInterceptor implements Interceptor {
    private final Logger logger = LoggerFactory.getLogger(RetryInterceptor.class);
    private final int maxRetries;
    private final int waitTimeInMs;
    private final ThreadSleep sleep;

    public RetryInterceptor(int maxRetries, Duration retryWaitTime, ThreadSleep sleep) {
        this.maxRetries = maxRetries;
        this.sleep = sleep;
        waitTimeInMs = (int) retryWaitTime.toMillis();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        int attempts = 0;
        while (true) {
            attempts++;
            try {
                Response response = chain.proceed(request);
                int statusCode = response.code();
                if (shouldRetry(attempts, statusCode)) {
                    logger.warn(errorCode("HTTP_REQUEST_FAILED"), "http request failed, retry soon, responseStatus={}, uri={}", statusCode, request.url());
                    closeRequestBody(response);
                    sleep.sleep(waitTime(attempts));
                    continue;
                }
                return response;
            } catch (IOException e) {
                if (shouldRetry(attempts, request.method(), e)) {
                    logger.warn(errorCode("HTTP_REQUEST_FAILED"), "http request failed, retry soon, uri={}", request.url(), e);
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
        return attempts < maxRetries
                && (statusCode == HTTPStatus.SERVICE_UNAVAILABLE.code || statusCode == HTTPStatus.TOO_MANY_REQUESTS.code);
    }

    boolean shouldRetry(int attempts, String method, IOException e) {
        if (attempts >= maxRetries) return false;

        // only not retry on POST with read time out
        // okHTTP uses both socket timeout and AsyncTimeout, it closes socket/connection when timeout is detected by background thread, so no need to close exchange
        // refer to SocketAsyncTimeout.timeout()
        if (!"POST".equals(method)) return true;
        // refer to AsyncTimeout.newTimeoutException() -> SocketAsyncTimeout.newTimeoutException()
        if (e instanceof SocketTimeoutException && "timeout".equals(e.getMessage())) return false;
        // it throws IOException("Canceled") hits callTimeout, refer to RetryAndFollowUpInterceptor.intercept()
        return !"Canceled".equals(e.getMessage());
    }

    Duration waitTime(int attempts) {
        return Duration.ofMillis(waitTimeInMs << attempts - 1);
    }

    public interface ThreadSleep {
        void sleep(Duration time);
    }
}
