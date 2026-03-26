package core.framework.internal.json;

import core.framework.json.JSONException;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectReader;


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

    @Nullable
    public T fromJSON(byte[] json) {
        try {
            return reader.readValue(json);
        } catch (JacksonException e) {
            throw new JSONException(e);
        }
    }

    @Nullable
    public T fromJSON(String json) {
        return reader.readValue(json);
    }
}
