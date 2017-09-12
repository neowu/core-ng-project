package core.framework.impl.web.response;

import core.framework.api.http.ContentType;
import core.framework.api.http.HTTPStatus;
import core.framework.api.util.Maps;
import core.framework.api.web.CookieSpec;
import core.framework.api.web.Response;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;

import java.util.Map;
import java.util.Optional;

/**
 * @author neo
 */
public final class ResponseImpl implements Response {
    public final Map<HttpString, String> headers = Maps.newHashMap();
    public final Body body;
    Map<CookieSpec, String> cookies;
    private HTTPStatus status = HTTPStatus.OK;
    private ContentType contentType;

    public ResponseImpl(Body body) {
        this.body = body;
    }

    @Override
    public HTTPStatus status() {
        return status;
    }

    @Override
    public Response status(HTTPStatus status) {
        this.status = status;
        return this;
    }

    @Override
    public Response header(String name, Object value) {
        headers.put(new HttpString(name), String.valueOf(value));
        return this;
    }

    @Override
    public Optional<ContentType> contentType() {
        return Optional.ofNullable(contentType);
    }

    @Override
    public Response contentType(ContentType contentType) {
        if (contentType != null) {
            this.contentType = contentType;
            headers.put(Headers.CONTENT_TYPE, contentType.toString());
        }
        return this;
    }

    @Override
    public Response cookie(CookieSpec spec, String value) {
        if (cookies == null) cookies = Maps.newHashMap();
        cookies.put(spec, value);
        return this;
    }
}
