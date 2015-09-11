package core.framework.api.search;

import core.framework.api.util.StopWatch;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * @author neo
 */
public final class ElasticSearch {
    private final Logger logger = LoggerFactory.getLogger(ElasticSearch.class);

    private final Client client;
    private final Duration slowQueryThreshold;

    ElasticSearch(Client client, Duration slowQueryThreshold) {
        this.client = client;
        this.slowQueryThreshold = slowQueryThreshold;
    }

    public <T> ElasticSearchType<T> type(String index, String type, Class<T> documentClass) {
        return new ElasticSearchType<>(client, index, type, documentClass, slowQueryThreshold);
    }

    public void close() {
        logger.info("close elastic search client");
        try {
            client.close();
        } catch (ElasticsearchException e) {
            logger.warn("failed to close elastic search client", e);
        }
    }

    public void createIndex(String index, String source) {
        StopWatch watch = new StopWatch();
        try {
            client.admin()
                .indices()
                .prepareCreate(index)
                .setSource(source)
                .get();
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            logger.debug("create index, index={}, elapsedTime={}", index, watch.elapsedTime());
        }
    }

    public void flush(String index) {
        StopWatch watch = new StopWatch();
        try {
            client.admin()
                .indices()
                .prepareFlush(index)
                .get();
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            logger.debug("flush, index={}, elapsedTime={}", index, watch.elapsedTime());
        }
    }
}
