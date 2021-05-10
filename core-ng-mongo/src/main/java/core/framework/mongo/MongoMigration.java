package core.framework.mongo;

import com.mongodb.ConnectionString;
import core.framework.internal.module.PropertyManager;
import core.framework.mongo.impl.MongoImpl;
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
        this(propertyFileClasspath, "sys.mongo.uri");
    }

    public MongoMigration(String propertyFileClasspath, String key) {
        var properties = new PropertyManager();
        properties.properties.load(propertyFileClasspath);
        uri = properties.property(key).orElseThrow();
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
