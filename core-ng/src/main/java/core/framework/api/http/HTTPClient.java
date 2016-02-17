package core.framework.api.http;

import core.framework.api.log.ActionLogContext;
import core.framework.api.log.Markers;
import core.framework.api.util.Charsets;
import core.framework.api.util.InputStreams;
import core.framework.api.util.Maps;
import core.framework.api.util.StopWatch;
import core.framework.impl.log.LogParam;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;

/**
 * @author neo
 */
public final class HTTPClient {
    static {
        // allow server ssl cert change during renegotiation
        // http client uses pooled connection, multiple requests to same host may hit different server behind LB
        System.setProperty("jdk.tls.allowUnsafeServerCertChange", "true");
    }

    private final Logger logger = LoggerFactory.getLogger(HTTPClient.class);

    private final CloseableHttpClient client;
    private final long slowOperationThresholdInMs;

    public HTTPClient(CloseableHttpClient client, long slowOperationThresholdInMs) {
        this.client = client;
        this.slowOperationThresholdInMs = slowOperationThresholdInMs;
    }

    public void close() {
        logger.info("close http client");
        try {
            client.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public HTTPResponse execute(HTTPRequest request) {
        StopWatch watch = new StopWatch();
        HttpUriRequest httpRequest = request.builder.build();
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
            HTTPResponse response = new HTTPResponse(HTTPStatus.parse(statusCode), headers, body);
            logResponseText(response);
            return response;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("http", elapsedTime);
            logger.debug("execute, elapsedTime={}", elapsedTime);
            if (elapsedTime > slowOperationThresholdInMs) {
                logger.warn(Markers.errorCode("SLOW_HTTP"), "slow http operation, elapsedTime={}", elapsedTime);
            }
        }
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
        ContentType contentType = response.contentType;
        if (contentType == null) return;
        String mediaType = contentType.mediaType();
        if (mediaType.contains("text") || mediaType.contains("json"))
            logger.debug("[response] body={}", LogParam.of(response.body(), contentType.charset().orElse(Charsets.UTF_8)));
    }
}
