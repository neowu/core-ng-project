package core.framework.http;

import core.framework.util.ASCII;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static core.framework.log.Markers.errorCode;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author neo
 */
public final class ContentType {
    public static final ContentType TEXT_HTML = create("text/html", UTF_8);
    public static final ContentType TEXT_CSS = create("text/css", UTF_8);
    public static final ContentType TEXT_PLAIN = create("text/plain", UTF_8);
    public static final ContentType TEXT_XML = create("text/xml", UTF_8);
    // refer to https://www.iana.org/assignments/media-types/application/json, No "charset" parameter is defined for application/json
    // https://tools.ietf.org/html/rfc7159#section-8.1, UTF-8 as default encoding for JSON
    // https://chromium-review.googlesource.com/c/chromium/src/+/587829
    public static final ContentType APPLICATION_JSON = create("application/json", null);
    public static final ContentType APPLICATION_JAVASCRIPT = create("application/javascript", UTF_8);
    // form body content type doesn't use charset normally, refer to https://www.w3.org/TR/html5/sec-forms.html#urlencoded-form-data
    public static final ContentType APPLICATION_FORM_URLENCODED = create("application/x-www-form-urlencoded", null);
    public static final ContentType APPLICATION_OCTET_STREAM = create("application/octet-stream", null);
    public static final ContentType IMAGE_PNG = create("image/png", null);

    private static final Logger LOGGER = LoggerFactory.getLogger(ContentType.class);
    // cache most used ones to save paring time
    private static final Map<String, ContentType> CACHE = Map.of(
            APPLICATION_JSON.contentType, APPLICATION_JSON,
            TEXT_HTML.contentType, TEXT_HTML
    );

    // only cover common case, assume pattern is "media-type; charset=" or "multipart/form-data; boundary="
    // according to https://www.w3.org/Protocols/rfc1341/4_Content-Type.html, content type value is case insensitive
    public static ContentType parse(String contentType) {
        String normalizedContentType = ASCII.toLowerCase(contentType);
        ContentType type = CACHE.get(normalizedContentType);
        if (type != null) return type;

        return parseContentType(normalizedContentType);
    }

    public static ContentType create(String mediaType, Charset charset) {
        String contentType = charset == null ? mediaType : mediaType + "; charset=" + ASCII.toLowerCase(charset.name());
        return new ContentType(contentType, mediaType, charset);
    }

    private static ContentType parseContentType(String contentType) {
        String mediaType = contentType;
        Charset charset = null;

        int firstSemicolon = contentType.indexOf(';');
        if (firstSemicolon > 0) {
            mediaType = contentType.substring(0, firstSemicolon);

            int charsetStartIndex = contentType.indexOf("charset=", firstSemicolon + 1);
            if (charsetStartIndex > 0) {
                int charsetEndIndex = contentType.indexOf(';', charsetStartIndex + 8);
                charset = parseCharset(contentType.substring(charsetStartIndex + 8, charsetEndIndex == -1 ? contentType.length() : charsetEndIndex));
            }
        }

        return new ContentType(contentType, mediaType, charset);
    }

    private static Charset parseCharset(String charset) {
        try {
            return Charset.forName(charset);
        } catch (UnsupportedCharsetException | IllegalCharsetNameException e) {
            LOGGER.warn(errorCode("INVALID_CONTENT_TYPE"), "ignore unsupported charset, charset={}", charset, e);
            return null;
        }
    }

    public final String mediaType;
    private final String contentType;
    private final Charset charset;

    private ContentType(String contentType, String mediaType, Charset charset) {
        this.contentType = contentType;
        this.mediaType = mediaType;
        this.charset = charset;
    }

    public Optional<Charset> charset() {
        return Optional.ofNullable(charset);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || ContentType.class != object.getClass()) return false;
        ContentType that = (ContentType) object;
        return contentType.equals(that.contentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contentType);
    }

    @Override
    public String toString() {
        return contentType;
    }
}
