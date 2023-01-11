package core.framework.mongo;

import com.mongodb.ConnectionString;
import core.framework.internal.module.PropertyManager;
import core.framework.mongo.impl.MongoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * @author neo
 */
public class MongoMigration {
    private final Logger logger = LoggerFactory.getLogger(MongoMigration.class);
    private final String uri;
    private final PropertyManager properties = new PropertyManager();

    public MongoMigration(String propertyFileClasspath) {
        properties.properties.load(propertyFileClasspath);
        uri = properties.property("sys.mongo.uri").orElseThrow();
    }

    public void migrate(Consumer<Mongo> consumer) {
        var mongo = new MongoImpl();
        try {
            mongo.uri = new ConnectionString(uri);
            mongo.timeout(Duration.ofHours(1)); // index building could take long
            mongo.initialize();
            consumer.accept(mongo);
        } catch (Throwable e) {
            logger.error("failed to run migration", e);
            throw e;
        } finally {
            mongo.close();
        }
    }

    public String requiredProperty(String key) {
        return properties.property(key).orElseThrow(() -> new Error("property key not found, key=" + key));
    }
}
