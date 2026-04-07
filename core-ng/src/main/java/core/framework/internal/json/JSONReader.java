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

    // according to benchmark, pass byte[] to reader is fastest, second is to convert string to byte[] then pass to reader
    @Nullable
    public T fromJSON(byte[] json) {
        try {
            return reader.readValue(json);
        } catch (JacksonException e) {
            // jackson exception contains source info, refer to StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION
            // not leak internal info to external, root cause can be viewed in trace
            throw new JSONException("failed to deserialize json, class=" + reader.getValueType().getRawClass().getCanonicalName(), e);
        }
    }
}
