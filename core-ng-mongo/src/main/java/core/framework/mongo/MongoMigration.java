package core.framework.mongo;

import com.mongodb.ConnectionString;
import core.framework.mongo.impl.MongoImpl;
import core.framework.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * @author neo
 */
public class MongoMigration {
    private final Logger logger = LoggerFactory.getLogger(MongoMigration.class);
    private final String uri;

    public MongoMigration(String propertyFileClasspath) {
        var properties = new Properties();
        properties.load(propertyFileClasspath);
        uri = properties.get("sys.mongo.uri").orElseThrow();
    }

    public void migrate(Consumer<Mongo> consumer) {
        var mongo = new MongoImpl();
        try {
            mongo.uri = new ConnectionString(uri);
            mongo.initialize();
            consumer.accept(mongo);
        } catch (Throwable e) {
            logger.error("failed to run migration", e);
            throw e;
        } finally {
            mongo.close();
        }
    }
}
