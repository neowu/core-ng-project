package core.framework.internal.http;

import core.framework.api.http.HTTPStatus;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import core.framework.util.Threads;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.connection.RouteException;
import okhttp3.internal.http2.ConnectionShutdownException;
import okhttp3.internal.http2.ErrorCode;
import okhttp3.internal.http2.StreamResetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
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

    public RetryInterceptor(int maxRetries, Duration retryWaitTime) {
        this.maxRetries = maxRetries;
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
                if (shouldRetry(statusCode, attempts)) {    // do not check call.isCanceled(), RetryAndFollowUpInterceptor already checked and throw exception
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
            sleep(waitTime(attempts));
            attempts++;

            // set to actionLog directly to keep trace log concise
            ActionLog actionLog = LogManager.CURRENT_ACTION_LOG.get();
            if (actionLog != null) actionLog.stats.compute("http_retries", (key, oldValue) -> (oldValue == null) ? 1.0 : oldValue + 1);
        }
    }

    // remove query params, to keep it simple by skipping masking
    String uri(Request request) {
        return request.url().newBuilder().query(null).build().toString();
    }

    // response.close asserts body not null, refer to Response.close()
    // RetryAndFollowUpInterceptor also closes body directly
    private void closeResponseBody(Response response) {
        ResponseBody body = response.body();
        if (body != null) closeQuietly(body);
    }

    boolean shouldRetry(int statusCode, int attempts) {
        if (statusCode == HTTPStatus.SERVICE_UNAVAILABLE.code || statusCode == HTTPStatus.TOO_MANY_REQUESTS.code) {
            if (attempts >= maxRetries) return false;
            return withinMaxProcessTime(attempts);
        }
        return false;
    }

    // refer to RetryAndFollowUpInterceptor.intercept for built-in error handling
    boolean shouldRetry(boolean canceled, String method, int attempts, Exception e) {
        if (canceled) return false;    // AsyncTimout cancels call if callTimeout, refer to RealCall.kt/timout field
        if (attempts >= maxRetries) return false;
        if (!withinMaxProcessTime(attempts)) return false;

        if (e instanceof RouteException) return true;   // if it's route failure, then request is not sent yet
        if (e instanceof ConnectionShutdownException) return true;  // refer to RetryAndFollowUpInterceptor -> requestSendStarted = e !is ConnectionShutdownException

        // only not retry on POST if request sent
        if (!"POST".equals(method)) return true;

        // should not retry on connection reset, the request could be sent already, and server side may continue to complete it
        if (e instanceof SSLException && "Connection reset".equals(e.getMessage())) return false;
        if (e instanceof StreamResetException exception && exception.errorCode == ErrorCode.CANCEL) return false;

        // okHTTP uses both socket timeout and AsyncTimeout, it closes socket/connection when timeout is detected by background thread, so no need to close exchange
        // refer to AsyncTimeout.newTimeoutException() -> SocketAsyncTimeout.newTimeoutException()
        return !(e instanceof SocketTimeoutException && "timeout".equals(e.getMessage()));
    }

    // for short circuit, e.g. heavy load request causes remote service busy, and client timeout triggers more retry requests, to amplify the load
    boolean withinMaxProcessTime(int attempts) {
        ActionLog actionLog = LogManager.CURRENT_ACTION_LOG.get();
        if (actionLog == null) return true;
        long remainingTime = actionLog.remainingProcessTimeInNano();
        if (remainingTime <= waitTime(attempts).toNanos()) {
            logger.debug("not retry due to max process time limit, remainingTime={}", Duration.ofNanos(remainingTime));
            return false;
        }
        return true;
    }

    Duration waitTime(int attempts) {
        return Duration.ofMillis(waitTimeInMs << attempts - 1);
    }

    void sleep(Duration duration) {
        Threads.sleepRoughly(duration);
    }
}
