package core.framework.impl.search;

import core.framework.api.log.ActionLogContext;
import core.framework.api.log.Markers;
import core.framework.api.search.AnalyzeRequest;
import core.framework.api.search.BulkDeleteRequest;
import core.framework.api.search.BulkIndexRequest;
import core.framework.api.search.DeleteRequest;
import core.framework.api.search.ElasticSearchType;
import core.framework.api.search.ForEach;
import core.framework.api.search.GetRequest;
import core.framework.api.search.Index;
import core.framework.api.search.IndexRequest;
import core.framework.api.search.SearchException;
import core.framework.api.search.SearchRequest;
import core.framework.api.search.SearchResponse;
import core.framework.api.util.Maps;
import core.framework.api.util.StopWatch;
import core.framework.impl.json.JSONReader;
import core.framework.impl.json.JSONWriter;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.sort.SortBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author neo
 */
public final class ElasticSearchTypeImpl<T> implements ElasticSearchType<T> {
    private final Logger logger = LoggerFactory.getLogger(ElasticSearchTypeImpl.class);

    private final ElasticSearchImpl elasticSearch;
    private final String index;
    private final String type;
    private final DocumentValidator<T> validator;
    private final long slowOperationThresholdInNanos;
    private final JSONReader<T> reader;
    private final JSONWriter<T> writer;

    ElasticSearchTypeImpl(ElasticSearchImpl elasticSearch, Class<T> documentClass, Duration slowOperationThreshold) {
        this.elasticSearch = elasticSearch;
        this.slowOperationThresholdInNanos = slowOperationThreshold.toNanos();
        Index index = documentClass.getDeclaredAnnotation(Index.class);
        this.index = index.index();
        this.type = index.type();
        validator = new DocumentValidator<>(documentClass);
        reader = JSONReader.of(documentClass);
        writer = JSONWriter.of(documentClass);
    }

    @Override
    public SearchResponse<T> search(SearchRequest request) {
        StopWatch watch = new StopWatch();
        long esTookTime = 0;
        String index = request.index == null ? this.index : request.index;
        try {
            SearchRequestBuilder builder = elasticSearch.client.prepareSearch(index)
                                                               .setQuery(request.query);
            if (request.type != null) builder.setSearchType(request.type);
            request.aggregations.forEach(builder::addAggregation);
            request.sorts.forEach(builder::addSort);
            if (request.skip != null) builder.setFrom(request.skip);
            if (request.limit != null) builder.setSize(request.limit);
            logger.debug("search, index={}, type={}, request={}", index, type, builder);
            org.elasticsearch.action.search.SearchResponse searchResponse = builder.get();
            esTookTime = searchResponse.getTook().nanos();
            if (searchResponse.getFailedShards() > 0) logger.warn("some shard failed, response={}", searchResponse);
            return searchResponse(searchResponse);
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime);
            logger.debug("search, esTookTime={}, elapsedTime={}", esTookTime, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    private SearchResponse<T> searchResponse(org.elasticsearch.action.search.SearchResponse response) {
        SearchHit[] hits = response.getHits().hits();
        List<T> items = new ArrayList<>(hits.length);
        for (SearchHit hit : hits) {
            items.add(reader.fromJSON(hit.source()));
        }
        Aggregations aggregationResponse = response.getAggregations();
        Map<String, Aggregation> aggregations = aggregationResponse == null ? Maps.newHashMap() : aggregationResponse.asMap();
        return new SearchResponse<>(items, response.getHits().totalHits(), aggregations);
    }

    @Override
    public Optional<T> get(GetRequest request) {
        StopWatch watch = new StopWatch();
        String index = request.index == null ? this.index : request.index;
        try {
            GetResponse response = elasticSearch.client.prepareGet(index, type, request.id).get();
            if (!response.isExists()) return Optional.empty();
            return Optional.of(reader.fromJSON(response.getSourceAsBytes()));
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime);
            logger.debug("get, index={}, type={}, id={}, elapsedTime={}", index, type, request.id, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public void index(IndexRequest<T> request) {
        StopWatch watch = new StopWatch();
        String index = request.index == null ? this.index : request.index;
        validator.validate(request.source);
        byte[] document = writer.toJSON(request.source);
        try {
            elasticSearch.client.prepareIndex(index, type, request.id)
                                .setSource(document)
                                .get();
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime);
            logger.debug("index, index={}, type={}, id={}, elapsedTime={}", index, type, request.id, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public void bulkIndex(BulkIndexRequest<T> request) {
        StopWatch watch = new StopWatch();
        String index = request.index == null ? this.index : request.index;
        BulkRequestBuilder builder = elasticSearch.client.prepareBulk();
        for (Map.Entry<String, T> entry : request.sources.entrySet()) {
            String id = entry.getKey();
            T source = entry.getValue();
            validator.validate(source);
            byte[] document = writer.toJSON(source);
            builder.add(elasticSearch.client.prepareIndex(index, type, id)
                                            .setSource(document));
        }
        long esTookTime = 0;
        try {
            BulkResponse response = builder.get();
            esTookTime = response.getTookInMillis();
            if (response.hasFailures()) throw new SearchException(response.buildFailureMessage());
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime);
            logger.debug("bulkIndex, index={}, type={}, size={}, esTookTime={}, elapsedTime={}", index, type, request.sources.size(), esTookTime, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public boolean delete(DeleteRequest request) {
        StopWatch watch = new StopWatch();
        String index = request.index == null ? this.index : request.index;
        try {
            DeleteResponse response = elasticSearch.client.prepareDelete(index, type, request.id).get();
            return response.getResult() == DocWriteResponse.Result.DELETED;
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime);
            logger.debug("delete, index={}, type={}, id={}, elapsedTime={}", index, type, request.id, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public void bulkDelete(BulkDeleteRequest request) {
        StopWatch watch = new StopWatch();
        String index = request.index == null ? this.index : request.index;
        BulkRequestBuilder builder = elasticSearch.client.prepareBulk();
        for (String id : request.ids) {
            builder.add(elasticSearch.client.prepareDelete(index, type, id));
        }
        long esTookTime = 0;
        try {
            BulkResponse response = builder.get();
            esTookTime = response.getTookInMillis();
            if (response.hasFailures()) throw new SearchException(response.buildFailureMessage());
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime);
            logger.debug("bulkDelete, index={}, type={}, size={}, esTookTime={}, elapsedTime={}", index, type, request.ids.size(), esTookTime, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public List<String> analyze(AnalyzeRequest request) {
        StopWatch watch = new StopWatch();
        String index = request.index == null ? this.index : request.index;
        try {
            AnalyzeResponse response = elasticSearch.client.admin().indices().prepareAnalyze(index, request.text).setAnalyzer(request.analyzer).get();
            return response.getTokens().stream().map(AnalyzeResponse.AnalyzeToken::getTerm).collect(Collectors.toList());
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime);
            logger.debug("analyze, index={}, analyzer={}, elapsedTime={}", index, request.analyzer, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public void forEach(ForEach<T> forEach) {
        if (forEach.consumer == null) throw new Error("forEach.consumer must not be null");
        if (forEach.query == null) throw new Error("forEach.query must not be null");
        if (forEach.scrollTimeout == null) throw new Error("forEach.scrollTimeout must not be null");
        if (forEach.limit == null || forEach.limit <= 0) throw new Error("forEach.limit must not be null and greater than 0");

        StopWatch watch = new StopWatch();
        TimeValue keepAlive = TimeValue.timeValueMillis(forEach.scrollTimeout.toMillis());
        long esTookTime = 0;
        String index = forEach.index == null ? this.index : forEach.index;
        try {
            SearchRequestBuilder builder = elasticSearch.client.prepareSearch(index)
                                                               .setQuery(forEach.query)
                                                               .addSort(SortBuilders.fieldSort("_doc"))
                                                               .setScroll(keepAlive)
                                                               .setSize(forEach.limit);
            logger.debug("foreach, index={}, type={}, request={}", index, type, builder);
            org.elasticsearch.action.search.SearchResponse searchResponse = builder.get();

            while (true) {
                esTookTime += searchResponse.getTookInMillis();
                if (searchResponse.getFailedShards() > 0) logger.warn("some shard failed, response={}", searchResponse);

                SearchHit[] hits = searchResponse.getHits().hits();
                if (hits.length == 0) break;

                for (SearchHit hit : hits) {
                    forEach.consumer.accept(reader.fromJSON(hit.source()));
                }

                String scrollId = searchResponse.getScrollId();
                searchResponse = elasticSearch.client.prepareSearchScroll(scrollId).setScroll(keepAlive).get();
            }
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime);
            logger.debug("foreach, esTookTime={}, elapsedTime={}", esTookTime, elapsedTime);
        }
    }

    private void checkSlowOperation(long elapsedTime) {
        if (elapsedTime > slowOperationThresholdInNanos) {
            logger.warn(Markers.errorCode("SLOW_ES"), "slow elasticsearch operation, elapsedTime={}", elapsedTime);
        }
    }
}
