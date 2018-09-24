package core.framework.mongo.impl;

import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

/**
 * @author neo
 */
class BsonLogParam {
    private final Bson bson;
    private final CodecRegistry registry;

    BsonLogParam(Bson bson, CodecRegistry registry) {
        this.bson = bson;
        this.registry = registry;
    }

    @Override
    public String toString() {
        if (bson == null) return "null";
        return bson.toBsonDocument(null, registry).toJson();
    }
}
