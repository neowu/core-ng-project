package core.framework.mongo.impl;

import core.framework.impl.log.filter.LogParam;
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
        if (bson == null) {
            builder.append("null");
        } else {
            builder.append(bson.toBsonDocument(null, registry).toJson());
        }
    }
}
