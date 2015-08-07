package core.framework.api.search;

import core.framework.api.log.ActionLogContext;
import core.framework.api.util.JSON;
import core.framework.api.util.StopWatch;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;

/**
 * @author neo
 */
public final class ElasticSearch {
    private final Logger logger = LoggerFactory.getLogger(ElasticSearch.class);

    private final Client client;
    private final String index;
    private final long slowQueryThresholdInMs;

    ElasticSearch(Client client, String index, Duration slowQueryThreshold) {
        this.client = client;
        this.index = index;
        this.slowQueryThresholdInMs = slowQueryThreshold.toMillis();
    }

    public void shutdown() {
        logger.info("shutdown elastic search client");
        try {
            client.close();
        } catch (ElasticsearchException e) {
            logger.warn("failed to close elastic search client", e);
        }
    }

    public void createIndex(String source) {
        StopWatch watch = new StopWatch();
        try {
            client.admin()
                .indices()
                .prepareCreate(index)
                .setSource(source)
                .get();
        } finally {
            logger.debug("create index, elapsedTime={}", watch.elapsedTime());
        }
    }

    public void flush() {
        StopWatch watch = new StopWatch();
        try {
            client.admin()
                .indices()
                .prepareFlush(index)
                .get();
        } finally {
            logger.debug("flush, elapsedTime={}", watch.elapsedTime());
        }
    }

    public void index(String type, String id, Object source) {
        index(type, id, null, source);
    }

    public void index(String type, String id, String parentId, Object source) {
        StopWatch watch = new StopWatch();
        try {
            String document = JSON.toJSON(source);
            client.prepareIndex(index, type)
                .setId(id)
                .setParent(parentId)
                .setSource(document)
                .get();
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime);
            logger.debug("index, type={}, id={}, elapsedTime={}", type, id, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    public void updateByScript(String type, String id, String script, Map<String, Object> params) {
        StopWatch watch = new StopWatch();
        try {
            client.prepareUpdate(index, type, id)
                .setScript(script, ScriptService.ScriptType.INLINE)
                .setScriptParams(params)
                .get();
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime);
            logger.debug("update by script, type={}, id={}, script={}, elapsedTime={}", type, id, script, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    public SearchResponse search(String type, SearchSourceBuilder source) {
        StopWatch watch = new StopWatch();
        long searchTimeTook = 0;
        try {
            SearchRequest request = new SearchRequest(index)
                .source(source)
                .types(type);
            SearchResponse response = client.search(request).actionGet();
            searchTimeTook = response.getTookInMillis();
            return response;
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime);
            logger.debug("search, type={}, query={}, searchTime={}, elapsedTime={}", type, source, searchTimeTook, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    private void checkSlowQuery(long elapsedTime) {
        if (elapsedTime > slowQueryThresholdInMs) logger.warn("slow query detected");
    }
}
