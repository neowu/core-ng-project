package core.framework.impl.http;

import core.framework.api.http.HTTPStatus;
import core.framework.http.HTTPClient;
import core.framework.http.HTTPClientException;
import core.framework.http.HTTPHeaders;
import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import core.framework.http.HTTPResponse;
import core.framework.impl.log.filter.MapLogParam;
import core.framework.log.ActionLogContext;
import core.framework.log.Markers;
import core.framework.util.Maps;
import core.framework.util.StopWatch;
import core.framework.util.Strings;
import core.framework.util.Threads;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.String.CASE_INSENSITIVE_ORDER;

/**
 * @author neo
 */
public final class HTTPClientImpl implements HTTPClient {
    private static final Map<Integer, HTTPStatus> HTTP_STATUSES;

    static {
        // allow server ssl cert change during renegotiation
        // http client uses pooled connection, multiple requests to same host may hit different server behind LB
        // refer to sun.security.ssl.ClientHandshakeContext, allowUnsafeServerCertChange = Utilities.getBooleanProperty("jdk.tls.allowUnsafeServerCertChange", false);
        System.setProperty("jdk.tls.allowUnsafeServerCertChange", "true");

        // api client keep alive should be shorter than server side in case server side disconnect connection first, use short value to release connection sooner in quiet time and still fit busy time
        // refer to jdk.internal.net.http.ConnectionPool
        System.setProperty("jdk.httpclient.keepalive.timeout", "15");   // 15s timeout for keep alive

        HTTPStatus[] values = HTTPStatus.values();
        HTTP_STATUSES = Maps.newHashMapWithExpectedSize(values.length);
        for (HTTPStatus status : values) {
            HTTP_STATUSES.put(status.code, status);
        }
    }

    static HTTPStatus parseHTTPStatus(int statusCode) {
        HTTPStatus status = HTTP_STATUSES.get(statusCode);
        if (status == null) throw new HTTPClientException("unsupported http status code, code=" + statusCode, "UNKNOWN_HTTP_STATUS_CODE");
        return status;
    }

    private final Logger logger = LoggerFactory.getLogger(HTTPClientImpl.class);
    private final HttpClient client;
    private final String userAgent;
    private final Duration timeout;
    private final int maxRetries;
    private final long slowOperationThresholdInNanos;

    public HTTPClientImpl(HttpClient client, String userAgent, Duration timeout, int maxRetries, Duration slowOperationThreshold) {
        this.client = client;
        this.userAgent = userAgent;
        this.timeout = timeout;
        this.maxRetries = maxRetries;
        slowOperationThresholdInNanos = slowOperationThreshold.toNanos();
    }

    @Override
    public HTTPResponse execute(HTTPRequest request) {
        var watch = new StopWatch();
        try {
            return executeWithRetry(request);
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("http", elapsed);
            logger.debug("execute, elapsed={}", elapsed);
            if (elapsed > slowOperationThresholdInNanos) {
                logger.warn(Markers.errorCode("SLOW_HTTP"), "slow http operation, elapsed={}", elapsed);
            }
        }
    }

    private HTTPResponse executeWithRetry(HTTPRequest request) {
        HttpRequest httpRequest = httpRequest(request);
        int attempts = 0;
        while (true) {
            attempts++;
            try {
                HttpResponse<byte[]> httpResponse = client.send(httpRequest, BodyHandlers.ofByteArray());
                HTTPResponse response = response(httpResponse);
                if (shouldRetry(attempts, request.method, null, response.status)) {
                    logger.warn(Markers.errorCode("HTTP_COMMUNICATION_FAILED"), "service unavailable, retry soon");
                    Threads.sleepRoughly(waitTime(attempts));
                    continue;
                }
                return response;
            } catch (IOException | InterruptedException e) {
                if (shouldRetry(attempts, request.method, e, null)) {
                    logger.warn(Markers.errorCode("HTTP_COMMUNICATION_FAILED"), "http communication failed, retry soon", e);
                    Threads.sleepRoughly(waitTime(attempts));
                    continue;
                }
                throw new HTTPClientException(e.getMessage(), "HTTP_COMMUNICATION_FAILED", e);
            }
        }
    }

    HTTPResponse response(HttpResponse<byte[]> httpResponse) {
        int statusCode = httpResponse.statusCode();
        logger.debug("[response] status={}", statusCode);
        Map<String, String> headers = new TreeMap<>(CASE_INSENSITIVE_ORDER);
        for (Map.Entry<String, List<String>> entry : httpResponse.headers().map().entrySet()) {
            String name = entry.getKey();
            if (!Strings.startsWith(name, ':')) {   // not put pseudo headers
                headers.put(name, entry.getValue().get(0));
            }
        }
        logger.debug("[response] headers={}", new MapLogParam(headers));

        byte[] body = httpResponse.body();
        HTTPStatus status = parseHTTPStatus(statusCode);
        var response = new HTTPResponse(status, headers, body);
        logger.debug("[response] body={}", BodyLogParam.param(body, response.contentType));
        return response;
    }

    HttpRequest httpRequest(HTTPRequest request) {
        HttpRequest.Builder builder = HttpRequest.newBuilder();

        try {
            var requestURI = new URI(requestURI(request.uri, request.params));
            builder.uri(requestURI);

            if ("https".equals(requestURI.getScheme())) builder.version(HttpClient.Version.HTTP_2);

            logger.debug("[request] method={}, uri={}", request.method, requestURI);
        } catch (URISyntaxException e) {
            throw new HTTPClientException("uri is invalid, uri=" + request.uri, "INVALID_URL", e);
        }

        if (!request.params.isEmpty())
            logger.debug("[request] params={}", new MapLogParam(request.params));   // due to null/empty will be serialized to empty value, so here to log actual params

        request.headers.put(HTTPHeaders.USER_AGENT, userAgent);
        for (Map.Entry<String, String> entry : request.headers.entrySet()) {
            builder.setHeader(entry.getKey(), entry.getValue());
        }
        logger.debug("[request] headers={}", new MapLogParam(request.headers));

        HttpRequest.BodyPublisher bodyPublisher;
        if (request.body != null) {
            logger.debug("[request] body={}", BodyLogParam.param(request.body, request.contentType));
            bodyPublisher = BodyPublishers.ofByteArray(request.body);
        } else {
            bodyPublisher = BodyPublishers.noBody();
        }
        builder.method(request.method.name(), bodyPublisher);

        builder.timeout(timeout);
        return builder.build();
    }

    private String requestURI(String uri, Map<String, String> params) {
        if (params.isEmpty()) return uri;

        var builder = new StringBuilder(256).append(uri).append('?');
        HTTPRequestHelper.urlEncoding(builder, params);
        return builder.toString();
    }

    boolean shouldRetry(int attempts, HTTPMethod method, Exception e, HTTPStatus status) {
        if (attempts >= maxRetries) return false;
        if (status == HTTPStatus.SERVICE_UNAVAILABLE) return true;
        if (e != null) {
            // POST is not idempotent, not retry on read time out
            return !(method == HTTPMethod.POST && e.getClass().equals(HttpTimeoutException.class));
        }
        return false;
    }

    Duration waitTime(int attempts) {
        return Duration.ofMillis(500 << attempts - 1);
    }
}
