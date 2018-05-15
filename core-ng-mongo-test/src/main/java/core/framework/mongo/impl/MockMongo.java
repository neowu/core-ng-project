package core.framework.mongo.impl;

import com.github.fakemongo.Fongo;
import com.mongodb.client.MongoDatabase;
import core.framework.util.StopWatch;
import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class MockMongo extends MongoImpl {
    private final Logger logger = LoggerFactory.getLogger(MockMongo.class);
    private final Fongo fongo = new Fongo("fongo");

    @Override
    MongoDatabase createDatabase(CodecRegistry registry) {
        StopWatch watch = new StopWatch();
        try {
            return fongo.getDatabase("test").withCodecRegistry(registry);
        } finally {
            logger.info("create mock mongo client, elapsedTime={}", watch.elapsedTime());
        }
    }
}
