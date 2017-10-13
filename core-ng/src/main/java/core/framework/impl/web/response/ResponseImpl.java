package core.framework.impl.web.response;

import core.framework.api.http.HTTPStatus;
import core.framework.http.ContentType;
import core.framework.util.Maps;
import core.framework.web.CookieSpec;
import core.framework.web.Response;
import io.undertow.util.HttpString;

import java.util.Map;
import java.util.Optional;

/**
 * @author neo
 */
public final class ResponseImpl implements Response {
    public final Body body;
    final Map<HttpString, String> headers = Maps.newHashMap();
    Map<CookieSpec, String> cookies;
    ContentType contentType;
    private HTTPStatus status = HTTPStatus.OK;

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
        this.contentType = contentType;
        return this;
    }

    @Override
    public Response cookie(CookieSpec spec, String value) {
        if (cookies == null) cookies = Maps.newHashMap();
        cookies.put(spec, value);
        return this;
    }
}
