package core.framework.test.mongo;

import com.github.fakemongo.Fongo;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import core.framework.impl.mongo.Mongo;
import org.bson.codecs.configuration.CodecRegistry;

/**
 * @author neo
 */
public class MockMongo extends Mongo {
    private final Fongo fongo = new Fongo("fongo");

    @Override
    protected MongoDatabase createDatabase(MongoClientURI uri, CodecRegistry registry) {
        return fongo.getDatabase(uri.getDatabase()).withCodecRegistry(registry);
    }

    @Override
    public void close() {

    }
}
