package core.framework.api.http;

import core.framework.api.util.Charsets;
import core.framework.api.util.Strings;

import java.nio.charset.Charset;
import java.util.Optional;

/**
 * @author neo
 */
public final class ContentType {
    public static final ContentType TEXT_HTML = new ContentType("text/html", Charsets.UTF_8);
    public static final ContentType TEXT_CSS = new ContentType("text/css", Charsets.UTF_8);
    public static final ContentType TEXT_PLAIN = new ContentType("text/plain", Charsets.UTF_8);
    public static final ContentType TEXT_XML = new ContentType("text/xml", Charsets.UTF_8);
    public static final ContentType APPLICATION_JSON = new ContentType("application/json", Charsets.UTF_8);
    public static final ContentType APPLICATION_JAVASCRIPT = new ContentType("application/javascript", Charsets.UTF_8);
    public static final ContentType APPLICATION_OCTET_STREAM = new ContentType("application/octet-stream", null);

    // only cover common case, assume pattern is "media-type; charset=",
    public static ContentType parse(String contentType) {
        int firstSemicolon = contentType.indexOf(';');
        if (firstSemicolon < 0) return new ContentType(contentType, null);
        int charsetIndex = contentType.indexOf("charset=", firstSemicolon + 1);
        return new ContentType(contentType.substring(0, firstSemicolon), Charset.forName(contentType.substring(charsetIndex + 8)));
    }

    private final String mediaType;
    private final Charset charset;

    public ContentType(String mediaType, Charset charset) {
        this.mediaType = mediaType;
        this.charset = charset;
    }

    public String value() {
        if (charset == null) return mediaType;
        return mediaType + "; charset=" + Strings.toLowerCase(charset.name());
    }

    public String mediaType() {
        return mediaType;
    }

    public Optional<Charset> charset() {
        return Optional.ofNullable(charset);
    }
}
