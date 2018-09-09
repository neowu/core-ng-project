package core.framework.impl.log.filter;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author neo
 */
public class BytesParam {
    private final byte[] bytes;

    public BytesParam(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public String toString() {
        return new String(bytes, UTF_8);
    }
}
