package core.framework.mongo.impl;

import core.framework.internal.log.filter.LogParam;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.Set;

/**
 * @author neo
 */
class BsonsLogParam implements LogParam {
    private final List<Bson> bsons;
    private final CodecRegistry registry;

    BsonsLogParam(List<Bson> bsons, CodecRegistry registry) {
        this.bsons = bsons;
        this.registry = registry;
    }

    @Override
    public void append(StringBuilder builder, Set<String> maskedFields, int maxParamLength) {
        builder.append('[');
        boolean first = true;
        for (Bson bson : bsons) {
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            new BsonLogParam(bson, registry)
                .append(builder, maskedFields, maxParamLength);
        }
        builder.append(']');
    }
}
