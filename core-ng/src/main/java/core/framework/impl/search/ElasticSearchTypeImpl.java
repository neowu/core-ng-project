package core.framework.impl.search;

import core.framework.api.log.ActionLogContext;
import core.framework.api.log.Markers;
import core.framework.api.search.ElasticSearchType;
import core.framework.api.search.SearchException;
import core.framework.api.util.JSON;
import core.framework.api.util.StopWatch;
import core.framework.api.util.Strings;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * @author neo
 */
public final class ElasticSearchTypeImpl<T> implements ElasticSearchType<T> {
    private final Logger logger = LoggerFactory.getLogger(ElasticSearchTypeImpl.class);

    private final Client client;
    private final String index;
    private final String type;
    private final DocumentValidator<T> validator;
    private final long slowQueryThresholdInMs;
    private final Class<T> documentClass;

    ElasticSearchTypeImpl(Client client, String index, String type, Class<T> documentClass, Duration slowQueryThreshold) {
        this.client = client;
        this.index = index;
        this.type = type;
        this.documentClass = documentClass;
        this.slowQueryThresholdInMs = slowQueryThreshold.toMillis();
        validator = new DocumentValidator<>(documentClass);
    }

    @Override
    public void index(String id, T source) {
        StopWatch watch = new StopWatch();
        validator.validate(source);
        try {
            String document = JSON.toJSON(source);
            client.prepareIndex(index, type)
                .setId(id)
                .setSource(Strings.bytes(document))
                .get();
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime);
            logger.debug("index, index={}, type={}, id={}, elapsedTime={}", index, type, id, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    @Override
    public void bulkIndex(Map<String, T> sources) {
        StopWatch watch = new StopWatch();
        long esTookTime = 0;
        try {
            BulkRequestBuilder builder = client.prepareBulk();
            for (Map.Entry<String, T> entry : sources.entrySet()) {
                String id = entry.getKey();
                T source = entry.getValue();
                validator.validate(source);
                String document = JSON.toJSON(source);
                builder.add(client.prepareIndex(index, type)
                    .setId(id)
                    .setSource(Strings.bytes(document)));
            }
            BulkResponse response = builder.get();
            esTookTime = response.getTookInMillis();
            if (response.hasFailures()) throw new SearchException(response.buildFailureMessage());
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime);
            logger.debug("bulkIndex, index={}, type={}, size={}, esTookTime={}, elapsedTime={}", index, type, sources.size(), esTookTime, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    @Override
    public Optional<T> get(String id) {
        StopWatch watch = new StopWatch();
        try {
            GetResponse response = client.prepareGet(index, type, id).get();
            if (!response.isExists()) return Optional.empty();
            return Optional.of(JSON.fromJSON(documentClass, response.getSourceAsString()));
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime);
            logger.debug("get, index={}, type={}, id={}, elapsedTime={}", index, type, id, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    @Override
    public void update(String id, UpdateRequest request) {
        StopWatch watch = new StopWatch();
        try {
            request.index(index)
                .type(type)
                .id(id);
            client.update(request).actionGet();
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime);
            logger.debug("update, index={}, type={}, id={}, elapsedTime={}", index, type, id, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    @Override
    public boolean delete(String id) {
        StopWatch watch = new StopWatch();
        try {
            DeleteResponse response = client.prepareDelete(index, type, id).get();
            return response.isFound();
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime);
            logger.debug("delete, index={}, type={}, id={}, elapsedTime={}", index, type, id, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    @Override
    public SearchResponse search(SearchSourceBuilder source) {
        StopWatch watch = new StopWatch();
        long esTookTime = 0;
        try {
            logger.debug("search, index={}, type={}, source={}", index, type, source);
            SearchRequest request = new SearchRequest(index)
                .source(source)
                .types(type);
            SearchResponse response = client.search(request).actionGet();
            esTookTime = response.getTookInMillis();
            if (response.getFailedShards() > 0) logger.warn("some shard failed, response={}", response);
            return response;
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime);
            logger.debug("search, esTookTime={}, elapsedTime={}", esTookTime, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    private void checkSlowQuery(long elapsedTime) {
        if (elapsedTime > slowQueryThresholdInMs) {
            logger.warn(Markers.errorCode("SLOW_QUERY"), "slow elasticsearch query, elapsedTime={}", elapsedTime);
        }
    }
}
