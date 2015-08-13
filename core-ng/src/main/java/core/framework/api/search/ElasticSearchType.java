package core.framework.api.search;

import core.framework.api.log.ActionLogContext;
import core.framework.api.util.JSON;
import core.framework.api.util.StopWatch;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * @author neo
 */
public final class ElasticSearchType<T> {
    private final Logger logger = LoggerFactory.getLogger(ElasticSearchType.class);

    private final Client client;
    private final String index;
    private final String type;
    private final long slowQueryThresholdInMs;

    ElasticSearchType(Client client, String index, String type, Duration slowQueryThreshold) {
        this.client = client;
        this.index = index;
        this.type = type;
        this.slowQueryThresholdInMs = slowQueryThreshold.toMillis();
    }

    public void index(String id, T source) {
        index(id, null, source);
    }

    public void index(String id, String parentId, T source) {
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
            logger.debug("index, index={}, type={}, id={}, elapsedTime={}", index, type, id, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    public void update(String id, T source) {
        StopWatch watch = new StopWatch();
        try {
            String document = JSON.toJSON(source);
            client.prepareUpdate(index, type, id)
                .setDoc(document)
                .get();
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime);
            logger.debug("update, index={}, type={}, id={}, elapsedTime={}", index, type, id, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    public void update(String id, UpdateRequest request) {
        StopWatch watch = new StopWatch();
        try {
            request.index(index)
                .type(type)
                .id(id);
            client.update(request).actionGet();
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime);
            logger.debug("update, index={}, type={}, id={}, elapsedTime={}", index, type, id, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    public SearchResponse search(SearchSourceBuilder source) {
        StopWatch watch = new StopWatch();
        long searchTime = 0;
        try {
            SearchRequest request = new SearchRequest(index)
                .source(source)
                .types(type);
            SearchResponse response = client.search(request).actionGet();
            searchTime = response.getTookInMillis();
            return response;
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime);
            logger.debug("search, index={}, type={}, searchTime={}, elapsedTime={}", index, type, searchTime, elapsedTime);
            if (elapsedTime > slowQueryThresholdInMs) {
                logger.warn("slow query detected, query={}", source);
            }
        }
    }

    private void checkSlowQuery(long elapsedTime) {
        if (elapsedTime > slowQueryThresholdInMs) logger.warn("slow query detected");
    }
}
