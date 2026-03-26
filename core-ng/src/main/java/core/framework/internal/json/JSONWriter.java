package core.framework.internal.json;

import core.framework.util.Strings;
import tools.jackson.databind.ObjectWriter;


/**
 * @author neo
 */
public final class JSONWriter<T> {
    private final ObjectWriter writer;

    public JSONWriter(Class<T> instanceClass) {
        this.writer = JSONMapper.OBJECT_MAPPER.writerFor(instanceClass);
    }

    // with jdk 11, write to String then covert to byte[] is faster than write to byte[]
    // toJSON won't throw exception especially instance class will be validated before
    public byte[] toJSON(T instance) {
        return Strings.bytes(writer.writeValueAsString(instance));
    }

    public String toJSONString(T instance) {
        return writer.writeValueAsString(instance);
    }
}
