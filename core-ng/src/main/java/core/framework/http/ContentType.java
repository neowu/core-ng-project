package core.framework.http;

import core.framework.util.ASCII;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author neo
 */
public final class ContentType {
    public static final ContentType TEXT_HTML = create("text/html", UTF_8);
    public static final ContentType TEXT_CSS = create("text/css", UTF_8);
    public static final ContentType TEXT_PLAIN = create("text/plain", UTF_8);
    public static final ContentType TEXT_XML = create("text/xml", UTF_8);
    public static final ContentType APPLICATION_JSON = create("application/json", UTF_8);
    public static final ContentType APPLICATION_JAVASCRIPT = create("application/javascript", UTF_8);
    // form body content type doesn't use charset normally, refer to https://www.w3.org/TR/html5/sec-forms.html#urlencoded-form-data
    public static final ContentType APPLICATION_FORM_URLENCODED = ContentType.create("application/x-www-form-urlencoded", null);
    public static final ContentType APPLICATION_OCTET_STREAM = create("application/octet-stream", null);
    public static final ContentType IMAGE_PNG = create("image/png", null);

    private static final Logger LOGGER = LoggerFactory.getLogger(ContentType.class);
    // cache most common ones to save paring time
    private static final Map<String, ContentType> CACHE = Map.of(
            APPLICATION_JSON.contentType, APPLICATION_JSON,
            TEXT_HTML.contentType, TEXT_HTML
    );

    // only cover common case, assume pattern is "media-type; charset=" or "multipart/form-data; boundary="
    public static ContentType parse(String contentType) {
        ContentType type = CACHE.get(contentType);
        if (type != null) return type;

        String mediaType = contentType;
        Charset charset = null;

        int firstSemicolon = contentType.indexOf(';');
        if (firstSemicolon > 0) {
            mediaType = contentType.substring(0, firstSemicolon);

            int charsetIndex = contentType.indexOf("charset=", firstSemicolon + 1);
            if (charsetIndex > 0) {
                charset = parseCharset(contentType.substring(charsetIndex + 8));
            }
        }

        return new ContentType(contentType, mediaType, charset);
    }

    public static ContentType create(String mediaType, Charset charset) {
        String contentType = charset == null ? mediaType : mediaType + "; charset=" + ASCII.toLowerCase(charset.name());
        return new ContentType(contentType, mediaType, charset);
    }

    private static Charset parseCharset(String charset) {
        try {
            return Charset.forName(charset);
        } catch (UnsupportedCharsetException e) {
            LOGGER.warn("ignore unsupported charset, charset={}", charset);
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
    public String toString() {
        return contentType;
    }
}
