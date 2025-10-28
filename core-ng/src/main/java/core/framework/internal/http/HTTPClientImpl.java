package core.framework.internal.http;

import core.framework.http.ContentType;
import core.framework.http.EventSource;
import core.framework.http.HTTPClient;
import core.framework.http.HTTPClientException;
import core.framework.http.HTTPHeaders;
import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import core.framework.http.HTTPResponse;
import core.framework.internal.log.filter.FieldMapLogParam;
import core.framework.log.ActionLogContext;
import core.framework.util.StopWatch;
import core.framework.util.Strings;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.String.CASE_INSENSITIVE_ORDER;

/**
 * @author neo
 */
public final class HTTPClientImpl implements HTTPClient {
    private static final MediaType MEDIA_TYPE_APPLICATION_JSON = MediaType.get(ContentType.APPLICATION_JSON.toString());
    public final long timeoutInNano;
    private final Logger logger = LoggerFactory.getLogger(HTTPClientImpl.class);
    private final String userAgent;
    private final OkHttpClient client;

    public HTTPClientImpl(OkHttpClient client, String userAgent, Duration timeout) {
        this.client = client;
        this.userAgent = userAgent;
        timeoutInNano = timeout.toNanos();
    }

    @Override
    public HTTPResponse execute(HTTPRequest request) {
        var watch = new StopWatch();
        Request httpRequest = httpRequest(request);
        int requestBodyLength = request.body == null ? 0 : request.body.length;
        int responseBodyLength = 0;
        try (Response httpResponse = client.newCall(httpRequest).execute()) {
            HTTPResponse response = response(httpResponse);
            responseBodyLength = response.body.length;
            return response;
        } catch (IOException e) {
            throw new HTTPClientException(Strings.format("http request failed, uri={}, error={}", request.uri, e.getMessage()), "HTTP_REQUEST_FAILED", e);
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("http", elapsed, 0, 0, responseBodyLength, requestBodyLength);
            logger.debug("execute, elapsed={}", elapsed);
        }
    }

    @Override
    public EventSource sse(HTTPRequest request) {
        var watch = new StopWatch();
        request.headers.put(HTTPHeaders.ACCEPT, "text/event-stream");
        int requestBodyLength = request.body == null ? 0 : request.body.length;
        Request httpRequest = httpRequest(request);
        try {
            Response httpResponse = client.newCall(httpRequest).execute();
            int statusCode = httpResponse.code();
            logger.debug("[response] status={}", statusCode);
            Map<String, @Nullable String> headers = headers(httpResponse);
            String contentType = headers.get(HTTPHeaders.CONTENT_TYPE);
            if (statusCode != 200 || contentType == null || !contentType.startsWith("text/event-stream")) {
                byte[] body = body(httpResponse, statusCode);
                logger.debug("[response] body={}", BodyLogParam.of(body, contentType == null ? null : ContentType.parse(contentType)));
                throw new HTTPClientException(Strings.format("invalid sse response, statusCode={}, content-type={}", statusCode, contentType), "HTTP_REQUEST_FAILED");
            }

            long elapsed = watch.elapsed();
            logger.debug("sse, elapsed={}", elapsed);
            return new EventSource(statusCode, headers, httpResponse.body(), requestBodyLength, elapsed);
        } catch (IOException e) {
            throw new HTTPClientException(Strings.format("sse request failed, uri={}, error={}", request.uri, e.getMessage()), "HTTP_REQUEST_FAILED", e);
        }
    }

    HTTPResponse response(Response httpResponse) throws IOException {
        int statusCode = httpResponse.code();
        logger.debug("[response] status={}", statusCode);
        Map<String, @Nullable String> headers = headers(httpResponse);
        byte[] body = body(httpResponse, statusCode);
        var response = new HTTPResponse(statusCode, headers, body);
        logger.debug("[response] body={}", BodyLogParam.of(body, response.contentType));
        return response;
    }

    Request httpRequest(HTTPRequest request) {
        var builder = new Request.Builder();

        try {
            logger.debug("[request] method={}, uri={}", request.method, request.uri);
            // not log final uri with query params which may contain sensitive data, to simplify so no need to do additional masking
            // the downside is the query param logged below may not appear in final requestURI if value is null or empty
            builder.url(request.requestURI());
        } catch (IllegalArgumentException e) {
            throw new HTTPClientException("uri is invalid, uri=" + request.uri, "INVALID_URL", e);
        }

        if (!request.params.isEmpty())
            logger.debug("[request] params={}", new FieldMapLogParam(request.params));   // due to null/empty will be serialized to empty value, so here to log actual params

        request.headers.put(HTTPHeaders.USER_AGENT, userAgent);
        for (Map.Entry<String, String> entry : request.headers.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
        logger.debug("[request] headers={}", new FieldMapLogParam(request.headers));

        if (request.body != null) {
            if (request.form != null) {
                logger.debug("[request] form={}", new FieldMapLogParam(request.form));
            } else {
                logger.debug("[request] body={}", BodyLogParam.of(request.body, request.contentType));
            }
            MediaType contentType = mediaType(request.contentType);
            builder.method(request.method.name(), RequestBody.create(request.body, contentType));
        } else {
            RequestBody body = request.method == HTTPMethod.GET || request.method == HTTPMethod.HEAD ? null : RequestBody.create(new byte[0], null);
            builder.method(request.method.name(), body);
        }

        if (request.connectTimeout != null || request.timeout != null) {
            logger.debug("[request] connectTimeout={}, timeout={}", request.connectTimeout, request.timeout);
            builder.tag(HTTPRequest.class, request);
        }

        return builder.build();
    }

    private byte[] body(Response httpResponse, int statusCode) throws IOException {
        byte[] body;
        if (statusCode == 204) {
            // refer to https://tools.ietf.org/html/rfc7230#section-3.3.2, with 204, server won't send body and content-length, hence no need to read it, and body will be quietly closed by response.close()
            body = new byte[0];
        } else {
            body = httpResponse.body().bytes();
        }
        return body;
    }

    private Map<String, @Nullable String> headers(Response httpResponse) {
        Map<String, String> headers = new TreeMap<>(CASE_INSENSITIVE_ORDER);
        Headers httpHeaders = httpResponse.headers();
        for (int i = 0; i < httpHeaders.size(); i++) {
            headers.put(httpHeaders.name(i), httpHeaders.value(i));
        }
        logger.debug("[response] headers={}", new FieldMapLogParam(headers));
        return headers;
    }

    @Nullable
    MediaType mediaType(@Nullable ContentType contentType) {
        if (contentType == null) return null;   // generally body is always set with valid content type, but in theory contentType=null is considered legitimate, so here to support such case
        if (ContentType.APPLICATION_JSON.equals(contentType)) return MEDIA_TYPE_APPLICATION_JSON; // avoid parsing as application/json is most used type
        return MediaType.get(contentType.toString());   // use get() not parse() to fail if passed invalid contentType
    }
}
