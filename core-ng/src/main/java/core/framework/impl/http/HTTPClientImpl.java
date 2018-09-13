package core.framework.impl.http;

import core.framework.api.http.HTTPStatus;
import core.framework.http.ContentType;
import core.framework.http.HTTPClient;
import core.framework.http.HTTPClientException;
import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import core.framework.http.HTTPResponse;
import core.framework.impl.log.filter.MapParam;
import core.framework.log.ActionLogContext;
import core.framework.log.Markers;
import core.framework.util.InputStreams;
import core.framework.util.Maps;
import core.framework.util.StopWatch;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Map;

/**
 * @author neo
 */
public final class HTTPClientImpl implements HTTPClient {
    private static final Map<Integer, HTTPStatus> HTTP_STATUSES;

    static {
        // allow server ssl cert change during renegotiation
        // http client uses pooled connection, multiple requests to same host may hit different server behind LB
        System.setProperty("jdk.tls.allowUnsafeServerCertChange", "true");

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
    private final CloseableHttpClient client;
    private final String userAgent;
    private final long slowOperationThresholdInNanos;

    public HTTPClientImpl(CloseableHttpClient client, String userAgent, Duration slowOperationThreshold) {
        this.client = client;
        this.userAgent = userAgent;
        slowOperationThresholdInNanos = slowOperationThreshold.toNanos();
    }

    @Override
    public HTTPResponse execute(HTTPRequest request) {
        var watch = new StopWatch();
        HttpUriRequest httpRequest = httpRequest(request);
        try (CloseableHttpResponse httpResponse = client.execute(httpRequest)) {
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            logger.debug("[response] status={}", statusCode);

            Map<String, String> headers = Maps.newHashMap();
            for (Header header : httpResponse.getAllHeaders()) {
                headers.putIfAbsent(header.getName(), header.getValue());
            }
            logger.debug("[response] headers={}", new MapParam(headers));

            HttpEntity entity = httpResponse.getEntity();
            byte[] body = responseBody(entity);
            var response = new HTTPResponse(parseHTTPStatus(statusCode), headers, body);
            logger.debug("[response] body={}", BodyParam.param(body, response.contentType().orElse(null)));
            return response;
        } catch (IOException | UncheckedIOException e) {
            throw new HTTPClientException(e.getMessage(), "HTTP_COMMUNICATION_FAILED", e);
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("http", elapsed);
            logger.debug("execute, elapsed={}", elapsed);
            if (elapsed > slowOperationThresholdInNanos) {
                logger.warn(Markers.errorCode("SLOW_HTTP"), "slow http operation, elapsed={}", elapsed);
            }
        }
    }

    @Override
    public void close() {
        logger.info("close http client, userAgent={}", userAgent);
        try {
            client.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    HttpUriRequest httpRequest(HTTPRequest request) {
        HTTPMethod method = request.method();
        String uri = request.uri();
        logger.debug("[request] method={}, uri={}", method, uri);
        RequestBuilder builder = RequestBuilder.create(method.name());
        try {
            builder.setUri(uri);
        } catch (IllegalArgumentException e) {
            throw new HTTPClientException("uri is invalid, uri=" + uri, "INVALID_URL", e);
        }

        Map<String, String> headers = request.headers();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.setHeader(entry.getKey(), entry.getValue());
        }
        logger.debug("[request] headers={}", new MapParam(headers));

        Map<String, String> params = request.params();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.addParameter(entry.getKey(), entry.getValue());
        }
        if (!params.isEmpty()) logger.debug("[request] params={}", new MapParam(params));

        byte[] body = request.body();
        if (body != null) {
            ContentType contentType = request.contentType();
            logger.debug("[request] contentType={}, body={}", contentType, BodyParam.param(body, contentType));
            org.apache.http.entity.ContentType type = org.apache.http.entity.ContentType.create(contentType.mediaType(), contentType.charset().orElse(null));
            builder.setEntity(new ByteArrayEntity(request.body(), type));
        }

        return builder.build();
    }

    byte[] responseBody(HttpEntity entity) throws IOException {
        if (entity == null) return new byte[0];  // for HEAD request, 204/304/205, http client will not create entity

        try (InputStream stream = entity.getContent()) {
            int length = (int) entity.getContentLength();
            if (length >= 0) {
                return InputStreams.bytesWithExpectedLength(stream, length);
            } else {
                return InputStreams.bytes(stream, 4096);
            }
        }
    }
}
