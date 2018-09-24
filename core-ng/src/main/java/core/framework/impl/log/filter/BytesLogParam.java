package core.framework.impl.log.filter;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author neo
 */
public class BytesLogParam {
    private final byte[] bytes;

    public BytesLogParam(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public String toString() {
        if (bytes == null) return null;
        return new String(bytes, UTF_8);
    }
}
