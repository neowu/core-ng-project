package core.framework.impl.log.param;

import core.framework.util.Charsets;

import java.nio.charset.Charset;

/**
 * @author neo
 */
public class BytesParam {
    private final byte[] bytes;
    private final Charset charset;

    public BytesParam(byte[] bytes) {
        this(bytes, Charsets.UTF_8);
    }

    public BytesParam(byte[] bytes, Charset charset) {
        this.bytes = bytes;
        this.charset = charset;
    }

    @Override
    public String toString() {
        return LogParamHelper.toString(bytes, charset, LogParamHelper.MAX_LONG_STRING_SIZE);
    }
}
