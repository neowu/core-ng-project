package core.framework.search;

import core.framework.search.impl.ElasticSearchImpl;
import core.framework.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * @author neo
 */
public class ElasticSearchMigration {
    private final Logger logger = LoggerFactory.getLogger(ElasticSearchMigration.class);
    private final String host;

    public ElasticSearchMigration(String propertyFileClasspath) {
        var properties = new Properties();
        properties.load(propertyFileClasspath);
        host = properties.get("sys.elasticsearch.host").orElseThrow();
    }

    public void migrate(Consumer<ElasticSearch> consumer) {
        var search = new ElasticSearchImpl();
        try {
            search.host = host;
            search.initialize();
            consumer.accept(search);
        } catch (Throwable e) {
            logger.error("failed to run migration", e);
        } finally {
            close(search);
        }
    }

    private void close(ElasticSearchImpl search) {
        try {
            search.close();
        } catch (IOException e) {
            logger.warn("failed to close elasticsearch client, error={}", e.getMessage(), e);
        }
    }
}
