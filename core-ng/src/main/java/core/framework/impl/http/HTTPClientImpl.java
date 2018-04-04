package core.framework.impl.http;

import core.framework.api.http.HTTPStatus;
import core.framework.http.ContentType;
import core.framework.http.HTTPClient;
import core.framework.http.HTTPClientException;
import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import core.framework.http.HTTPResponse;
import core.framework.impl.log.filter.BytesParam;
import core.framework.impl.log.filter.FieldParam;
import core.framework.impl.log.filter.JSONParam;
import core.framework.log.ActionLogContext;
import core.framework.log.Markers;
import core.framework.util.Charsets;
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
import java.util.HashMap;
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
        HTTP_STATUSES = new HashMap<>(values.length);
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
        StopWatch watch = new StopWatch();
        HttpUriRequest httpRequest = httpRequest(request);
        try (CloseableHttpResponse httpResponse = client.execute(httpRequest)) {
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            logger.debug("[response] status={}", statusCode);

            Map<String, String> headers = Maps.newHashMap();
            for (Header header : httpResponse.getAllHeaders()) {
                logger.debug("[response:header] {}={}", header.getName(), header.getValue());
                headers.putIfAbsent(header.getName(), header.getValue());
            }

            HttpEntity entity = httpResponse.getEntity();
            byte[] body = responseBody(entity);
            HTTPResponse response = new HTTPResponse(parseHTTPStatus(statusCode), headers, body);
            logResponseText(response);
            return response;
        } catch (IOException | UncheckedIOException e) {
            throw new HTTPClientException(e.getMessage(), "HTTP_COMMUNICATION_FAILED", e);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("http", elapsedTime);
            logger.debug("execute, elapsedTime={}", elapsedTime);
            if (elapsedTime > slowOperationThresholdInNanos) {
                logger.warn(Markers.errorCode("SLOW_HTTP"), "slow http operation, elapsedTime={}", elapsedTime);
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

        request.headers().forEach((name, value) -> {
            logger.debug("[request:header] {}={}", name, new FieldParam(name, value));
            builder.setHeader(name, value);
        });

        request.params().forEach((name, value) -> {
            logger.debug("[request:param] {}={}", name, value);
            builder.addParameter(name, value);
        });

        byte[] body = request.body();
        if (body != null) {
            ContentType contentType = request.contentType();
            logRequestBody(request, contentType);
            org.apache.http.entity.ContentType type = org.apache.http.entity.ContentType.create(contentType.mediaType(), contentType.charset().orElse(null));
            builder.setEntity(new ByteArrayEntity(request.body(), type));
        }

        return builder.build();
    }

    private void logRequestBody(HTTPRequest request, ContentType contentType) {
        Object bodyParam;
        if (ContentType.APPLICATION_JSON.mediaType().equals(contentType.mediaType())) {
            bodyParam = new JSONParam(request.body(), contentType.charset().orElse(Charsets.UTF_8));
        } else {
            bodyParam = new BytesParam(request.body());
        }
        logger.debug("[request] contentType={}, body={}", contentType, bodyParam);
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

    private void logResponseText(HTTPResponse response) {
        response.contentType().ifPresent(contentType -> {
            String mediaType = contentType.mediaType();
            if (mediaType.contains("text") || mediaType.contains("json")) {
                logger.debug("[response] body={}", new BytesParam(response.body(), contentType.charset().orElse(Charsets.UTF_8)));
            }
        });
    }
}
