package core.framework.internal.json;

import core.framework.json.JSONException;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;


/**
 * @author neo
 */
public final class JSONReader<T> {
    // used by JSONReader to read internal json (request/response/message/cache), reject NUL and lone surrogate in string
    // OBJECT_MAPPER (used by JSON.fromJSON) stays lenient, as it may parse external json
    static final JsonMapper STRICT_OBJECT_MAPPER = JSONMapper.builder().addModule(strictStringModule()).build();

    static JacksonModule strictStringModule() {
        return new SimpleModule("strictString")
            .addDeserializer(String.class, new StrictStringDeserializer())
            .addKeyDeserializer(String.class, new StrictStringDeserializer.StrictStringKeyDeserializer());
    }

    // used internally, performance is top priority in design, reader is about 3~6% faster than mapper since type is pre determined
    // refer to https://github.com/FasterXML/jackson-docs/wiki/Presentation:-Jackson-Performance
    private final ObjectReader reader;

    public JSONReader(Class<T> instanceClass) {
        this.reader = STRICT_OBJECT_MAPPER.readerFor(instanceClass);
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
