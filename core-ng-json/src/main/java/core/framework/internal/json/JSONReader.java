package core.framework.internal.json;

import com.fasterxml.jackson.databind.ObjectReader;

import java.io.IOException;


/**
 * @author neo
 */
public final class JSONReader<T> {
    // used internally, performance is top priority in design, reader is about 3~6% faster than mapper since type is pre determined
    // refer to https://github.com/FasterXML/jackson-docs/wiki/Presentation:-Jackson-Performance
    private final ObjectReader reader;

    public JSONReader(Class<T> instanceClass) {
        this.reader = JSONMapper.OBJECT_MAPPER.readerFor(instanceClass);
    }

    public T fromJSON(byte[] json) throws IOException {
        return reader.readValue(json);
    }

    public T fromJSON(String json) throws IOException {
        return reader.readValue(json);
    }
}
