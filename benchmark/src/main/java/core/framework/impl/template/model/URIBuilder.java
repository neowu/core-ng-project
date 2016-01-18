package core.framework.impl.template.model;

import core.framework.api.util.Encodings;
import core.framework.api.util.Exceptions;

/**
 * @author neo
 */
public class URIBuilder {
    private final StringBuilder uri;
    private boolean queryStarted;

    public URIBuilder() {
        uri = new StringBuilder();
    }

    public URIBuilder(String prefix) {
        uri = new StringBuilder(prefix);
        queryStarted = prefix.indexOf('?') > 0;
    }

    public URIBuilder addPath(String segment) {
        if (queryStarted) throw Exceptions.error("path segment must not be added after query, uri={}", uri.toString());
        if (uri.length() > 0 && uri.charAt(uri.length() - 1) != '/') uri.append('/');
        uri.append(Encodings.uriComponent(segment));
        return this;
    }

    public URIBuilder addQueryParam(String name, String value) {
        uri.append(queryStarted ? '&' : '?').append(Encodings.uriComponent(name)).append('=').append(Encodings.uriComponent(value));
        queryStarted = true;
        return this;
    }

    public String toURI() {
        return uri.toString();
    }
}
