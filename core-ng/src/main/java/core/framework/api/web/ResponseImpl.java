package core.framework.api.web;

import core.framework.api.http.ContentType;
import core.framework.api.http.HTTPStatus;
import core.framework.api.util.Maps;
import core.framework.impl.web.response.Body;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;

import java.util.Map;

/**
 * @author neo
 */
public final class ResponseImpl implements Response {
    public final Map<HttpString, String> headers = Maps.newHashMap();
    public final Body body;
    public Map<CookieSpec, String> cookies;
    private HTTPStatus status = HTTPStatus.OK;

    ResponseImpl(Body body) {
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
    public Response cookie(CookieSpec spec, String value) {
        if (cookies == null) cookies = Maps.newHashMap();
        cookies.put(spec, value);
        return this;
    }

    @Override
    public Response contentType(ContentType contentType) {
        if (contentType != null)
            headers.put(Headers.CONTENT_TYPE, contentType.value());
        return this;
    }
}
