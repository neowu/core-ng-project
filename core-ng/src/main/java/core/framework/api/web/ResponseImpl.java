package core.framework.api.web;

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
    private HTTPStatus status = HTTPStatus.OK;
    public final Map<HttpString, String> headers = Maps.newHashMap();
    public Map<CookieSpec, String> cookies;
    public final Body body;

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
    public Response contentType(String contentType) {
        if (contentType != null)
            headers.put(Headers.CONTENT_TYPE, contentType);
        return this;
    }
}
