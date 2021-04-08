package core.framework.search;

import core.framework.internal.module.PropertyManager;
import core.framework.search.impl.ElasticSearchHost;
import core.framework.search.impl.ElasticSearchImpl;
import core.framework.search.impl.log.ESLoggerContextFactory;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * @author neo
 */
public class ElasticSearchMigration {
    private final Logger logger = LoggerFactory.getLogger(ElasticSearchMigration.class);
    private final HttpHost[] hosts;

    public ElasticSearchMigration(String propertyFileClasspath) {
        // setup logger
        System.setProperty(LogManager.FACTORY_PROPERTY_NAME, ESLoggerContextFactory.class.getName());

        var properties = new PropertyManager();
        properties.properties.load(propertyFileClasspath);
        String host = properties.property("sys.elasticsearch.host").orElseThrow();
        hosts = ElasticSearchHost.parse(host);
    }

    public void migrate(Consumer<ElasticSearch> consumer) {
        var search = new ElasticSearchImpl();
        try {
            search.hosts = hosts;
            search.initialize();
            consumer.accept(search);
        } catch (Throwable e) {
            logger.error("failed to run migration", e);
            throw e;
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
