package core.framework.api.mongo;

import com.github.fakemongo.Fongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class MockMongoBuilder extends MongoBuilder {
    private final Logger logger = LoggerFactory.getLogger(MockMongoBuilder.class);

    @Override
    protected Mongo createMongo(MongoClientURI uri) {
        logger.info("create mock mongodb client, uri={}", uri);
        MongoClient mongoClient = new Fongo(String.valueOf(uri)).getMongo();
        CodecRegistry codec = uri.getOptions().getCodecRegistry();
        MongoDatabase database = mongoClient.getDatabase(uri.getDatabase())
            .withCodecRegistry(codec);
        return new Mongo(null, database);
    }
}
