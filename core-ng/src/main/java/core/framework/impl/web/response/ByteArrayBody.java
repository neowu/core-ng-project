package core.framework.impl.web.response;

import core.framework.api.http.ContentType;

/**
 * @author rainbow.cai
 */
public class ByteArrayBody implements Body {
    final byte[] bytes;
    final ContentType contentType;

    public ByteArrayBody(byte[] bytes, ContentType contentType) {
        this.bytes = bytes;
        this.contentType = contentType;
    }
}
