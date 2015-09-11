package core.framework.api.search;

import core.framework.api.log.ActionLogContext;
import core.framework.api.util.JSON;
import core.framework.api.util.StopWatch;
import core.framework.api.util.Strings;
import core.framework.impl.search.DocumentValidator;
import org.elasticsearch.ElasticsearchException;
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

    ElasticSearchType(Client client, String index, String type, Class<T> documentClass, Duration slowQueryThreshold) {
        this.client = client;
        this.index = index;
        this.type = type;
        this.documentClass = documentClass;
        this.slowQueryThresholdInMs = slowQueryThreshold.toMillis();
        validator = new DocumentValidator<>(documentClass);
    }

    public void index(String id, T source) {
        index(id, null, source);
    }

    public void index(String id, String parentId, T source) {
        StopWatch watch = new StopWatch();
        validator.validate(source);
        try {
            String document = JSON.toJSON(source);
            client.prepareIndex(index, type)
                .setId(id)
                .setParent(parentId)
                .setSource(Strings.bytes(document))
                .get();
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime);
            logger.debug("index, index={}, type={}, id={}, parentId={}, elapsedTime={}", index, type, id, parentId, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    public Optional<T> get(String id) {
        return get(id, null);
    }

    public Optional<T> get(String id, String parentId) {
        StopWatch watch = new StopWatch();
        try {
            GetResponse response = client.prepareGet(index, type, id).setParent(parentId).get();
            if (!response.isExists()) return Optional.empty();
            return Optional.of(JSON.fromJSON(documentClass, response.getSourceAsString()));
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime);
            logger.debug("get, index={}, type={}, id={}, parentId={}, elapsedTime={}", index, type, id, parentId, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    public void update(String id, UpdateRequest request) {
        update(id, null, request);
    }

    public void update(String id, String parentId, UpdateRequest request) {
        StopWatch watch = new StopWatch();
        try {
            request.index(index)
                .type(type)
                .id(id)
                .parent(parentId);
            client.update(request).actionGet();
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime);
            logger.debug("update, index={}, type={}, id={}, parentId={}, elapsedTime={}", index, type, id, parentId, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    public void delete(String id) {
        delete(id, null);
    }

    public void delete(String id, String parentId) {
        StopWatch watch = new StopWatch();
        try {
            client.prepareDelete(index, type, id).setParent(parentId).get();
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime);
            logger.debug("delete, index={}, type={}, id={}, parentId={}, elapsedTime={}", index, type, id, parentId, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    public SearchResponse search(SearchSourceBuilder source) {
        StopWatch watch = new StopWatch();
        long searchTime = 0;
        try {
            logger.debug("search, index={}, type={}, source={}", index, type, source);
            SearchRequest request = new SearchRequest(index)
                .source(source)
                .types(type);
            SearchResponse response = client.search(request).actionGet();
            searchTime = response.getTookInMillis();
            if (response.getFailedShards() > 0) logger.warn("some shard failed, response={}", response);
            return response;
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime);
            logger.debug("search, searchTime={}, elapsedTime={}", searchTime, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    private void checkSlowQuery(long elapsedTime) {
        if (elapsedTime > slowQueryThresholdInMs)
            logger.warn("slow query detected");
    }
}
