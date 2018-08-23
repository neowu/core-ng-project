package core.framework.impl.log.filter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author neo
 */
public class BytesParam {
    private final byte[] bytes;
    private final Charset charset;

    public BytesParam(byte[] bytes) {
        this(bytes, StandardCharsets.UTF_8);
    }

    public BytesParam(byte[] bytes, Charset charset) {
        this.bytes = bytes;
        this.charset = charset;
    }

    @Override
    public String toString() {
        return new String(bytes, charset);
    }
}
