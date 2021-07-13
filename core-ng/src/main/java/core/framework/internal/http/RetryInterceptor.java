package core.framework.internal.http;

import core.framework.api.http.HTTPStatus;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.connection.RouteException;
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
    private final long waitTimeInMs;
    private final ThreadSleep sleep;

    public RetryInterceptor(int maxRetries, Duration retryWaitTime, ThreadSleep sleep) {
        this.maxRetries = maxRetries;
        this.sleep = sleep;
        waitTimeInMs = retryWaitTime.toMillis();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        int attempts = 1;
        while (true) {
            try {
                Response response = chain.proceed(request);
                int statusCode = response.code();
                if (shouldRetry(chain.call().isCanceled(), attempts, statusCode)) {
                    logger.warn(errorCode("HTTP_REQUEST_FAILED"), "http request failed, retry soon, responseStatus={}, uri={}", statusCode, uri(request));
                    closeResponseBody(response);
                } else {
                    return response;
                }
            } catch (IOException | RouteException e) {
                if (shouldRetry(chain.call().isCanceled(), request.method(), attempts, e)) {
                    logger.warn(errorCode("HTTP_REQUEST_FAILED"), "http request failed, retry soon, uri={}, error={}", uri(request), e.getMessage(), e);
                } else {
                    throw e;
                }
            }
            sleep.sleep(waitTime(attempts));
            attempts++;

            // set to actionLog directly to keep trace log concise
            ActionLog actionLog = LogManager.CURRENT_ACTION_LOG.get();
            if (actionLog != null) actionLog.stats.compute("http_retries", (key, oldValue) -> (oldValue == null) ? 1.0 : oldValue + 1);
        }
    }

    boolean withinMaxProcessTime(int attempts) {
        ActionLog actionLog = LogManager.CURRENT_ACTION_LOG.get();
        if (actionLog == null || actionLog.maxProcessTimeInNano == -1) return true;
        long remainingTime = actionLog.remainingProcessTimeInNano();
        if (remainingTime < waitTime(attempts).toNanos()) {
            logger.debug("not retry due to max process time limit, maxProcessTime={}, timeLeft={}", Duration.ofNanos(actionLog.maxProcessTimeInNano), Duration.ofNanos(remainingTime));
            return false;
        }
        return true;
    }

    // remove query params, to keep it simple by skipping masking
    String uri(Request request) {
        return request.url().newBuilder().query(null).build().toString();
    }

    private void closeResponseBody(Response response) {
        ResponseBody body = response.body();
        if (body != null) closeQuietly(body);
    }

    boolean shouldRetry(boolean canceled, int attempts, int statusCode) {
        if (canceled) return false;    // AsyncTimout cancels call if callTimeout, refer to RealCall.kt/timout field
        if (attempts >= maxRetries) return false;
        if (!withinMaxProcessTime(attempts)) return false;

        return statusCode == HTTPStatus.SERVICE_UNAVAILABLE.code || statusCode == HTTPStatus.TOO_MANY_REQUESTS.code;
    }

    // refer to RetryAndFollowUpInterceptor.intercept for built-in error handling
    boolean shouldRetry(boolean canceled, String method, int attempts, Exception e) {
        if (canceled) return false;    // AsyncTimout cancels call if callTimeout, refer to RealCall.kt/timout field
        if (attempts >= maxRetries) return false;
        if (!withinMaxProcessTime(attempts)) return false;

        if (e instanceof RouteException) return true;   // if it's route failure, then request is not sent yet

        // only not retry on POST with read time out
        // okHTTP uses both socket timeout and AsyncTimeout, it closes socket/connection when timeout is detected by background thread, so no need to close exchange
        // refer to AsyncTimeout.newTimeoutException() -> SocketAsyncTimeout.newTimeoutException()
        return !("POST".equals(method) && e instanceof SocketTimeoutException && "timeout".equals(e.getMessage()));
    }

    Duration waitTime(int attempts) {
        return Duration.ofMillis(waitTimeInMs << attempts - 1);
    }

    public interface ThreadSleep {
        void sleep(Duration time);
    }
}
