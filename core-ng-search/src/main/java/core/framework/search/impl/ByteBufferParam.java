package core.framework.search.impl;

import core.framework.internal.log.filter.BytesLogParam;
import core.framework.internal.log.filter.LogParam;

import java.nio.ByteBuffer;
import java.util.Set;

public class ByteBufferParam implements LogParam {
    private final ByteBuffer buffer;

    public ByteBufferParam(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void append(StringBuilder builder, Set<String> maskedFields, int maxParamLength) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        new BytesLogParam(bytes).append(builder, maskedFields, maxParamLength);
    }
}
