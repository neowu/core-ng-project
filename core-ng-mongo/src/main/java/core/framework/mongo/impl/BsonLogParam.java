package core.framework.mongo.impl;

import core.framework.internal.log.filter.LogParam;
import org.bson.codecs.configuration.CodecConfigurationException;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import java.util.Set;

/**
 * @author neo
 */
class BsonLogParam implements LogParam {
    private final Bson bson;
    private final CodecRegistry registry;

    BsonLogParam(Bson bson, CodecRegistry registry) {
        this.bson = bson;
        this.registry = registry;
    }

    @Override
    public void append(StringBuilder builder, Set<String> maskedFields, int maxParamLength) {
        String value = logValue();
        builder.append(value);
    }

    String logValue() {
        if (bson == null) {
            return "null";
        } else {
            try {
                return bson.toBsonDocument(null, registry).toJson();
            } catch (CodecConfigurationException e) {
                return bson.toString(); // if can't find codec, fallback to toString to log, e.g. Updates/Filters may use unregistered enum codec
            }
        }
    }
}
