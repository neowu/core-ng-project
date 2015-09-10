package core.framework.api.search;

import core.framework.api.log.ActionLogContext;
import core.framework.api.util.JSON;
import core.framework.api.util.StopWatch;
import core.framework.impl.search.DocumentValidator;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Optional;

/**
 * @author neo
 */
public final class ElasticSearchType<T> {
    private final Logger logger = LoggerFactory.getLogger(ElasticSearchType.class);

    private final Client client;
    private final String index;
    private final String type;
    private final DocumentValidator<T> validator;
    private final long slowQueryThresholdInMs;
    private final Class<T> documentClass;

    ElasticSearchType(Client client, String index, String type, Class<T> documentClass, DocumentValidator<T> validator, Duration slowQueryThreshold) {
        this.client = client;
        this.index = index;
        this.type = type;
        this.documentClass = documentClass;
        this.validator = validator;
        this.slowQueryThresholdInMs = slowQueryThreshold.toMillis();
    }

    public void index(String id, T source) {
        StopWatch watch = new StopWatch();
        validator.validate(source);
        try {
            String document = JSON.toJSON(source);
            client.prepareIndex(index, type)
                .setId(id)
                .setSource(document)
                .get();
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime);
            logger.debug("index, index={}, type={}, id={}, elapsedTime={}", index, type, id, elapsedTime);
            if (elapsedTime > slowQueryThresholdInMs)
                logger.warn("slow query detected");
        }
    }

    public Optional<T> get(String id) {
        StopWatch watch = new StopWatch();
        try {
            GetResponse response = client.prepareGet(index, type, id).get();
            if (!response.isExists()) return Optional.empty();
            return Optional.of(JSON.fromJSON(documentClass, response.getSourceAsString()));
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime);
            logger.debug("get, index={}, type={}, id={}, elapsedTime={}", index, type, id, elapsedTime);
            if (elapsedTime > slowQueryThresholdInMs)
                logger.warn("slow query detected");
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
            if (elapsedTime > slowQueryThresholdInMs)
                logger.warn("slow query detected");
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
            logger.debug("search, index={}, type={}, searchTime={}, elapsedTime={}, query={}", index, type, searchTime, elapsedTime, source);
            if (elapsedTime > slowQueryThresholdInMs)
                logger.warn("slow query detected, query={}", source);
        }
    }
}
