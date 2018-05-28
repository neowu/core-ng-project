package core.framework.search.impl;

import core.framework.impl.json.JSONReader;
import core.framework.impl.json.JSONWriter;
import core.framework.log.ActionLogContext;
import core.framework.log.Markers;
import core.framework.search.AnalyzeRequest;
import core.framework.search.BulkDeleteRequest;
import core.framework.search.BulkIndexRequest;
import core.framework.search.CompleteRequest;
import core.framework.search.DeleteByQueryRequest;
import core.framework.search.DeleteRequest;
import core.framework.search.ElasticSearchType;
import core.framework.search.ForEach;
import core.framework.search.GetRequest;
import core.framework.search.Index;
import core.framework.search.IndexRequest;
import core.framework.search.SearchException;
import core.framework.search.SearchRequest;
import core.framework.search.SearchResponse;
import core.framework.util.Exceptions;
import core.framework.util.Maps;
import core.framework.util.StopWatch;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
        validate(request);

        StopWatch watch = new StopWatch();
        long esTookTime = 0;
        String index = request.index == null ? this.index : request.index;
        long hits = 0;
        try {
            SearchRequestBuilder builder = client().prepareSearch(index).setQuery(request.query);
            if (request.type != null) builder.setSearchType(request.type);
            request.aggregations.forEach(builder::addAggregation);
            request.sorts.forEach(builder::addSort);
            if (request.skip != null) builder.setFrom(request.skip);
            if (request.limit != null) builder.setSize(request.limit);
            logger.debug("search, index={}, type={}, request={}", index, type, builder);
            org.elasticsearch.action.search.SearchResponse searchResponse = builder.get();
            hits = searchResponse.getHits().getTotalHits();
            esTookTime = searchResponse.getTook().nanos();
            if (searchResponse.getFailedShards() > 0) logger.warn("some shard failed, response={}", searchResponse);
            return searchResponse(searchResponse);
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime, (int) hits, 0);
            logger.debug("search, hits={}, esTookTime={}, elapsedTime={}", hits, esTookTime, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    private void validate(SearchRequest request) {
        int skip = request.skip == null ? 0 : request.skip;
        int limit = request.limit == null ? 0 : request.limit;
        if (skip + limit > 10000)
            throw Exceptions.error("result window is too large, skip + limit must be less than or equal to 10000, skip={}, limit={}", request.skip, request.limit);
    }

    private SearchResponse<T> searchResponse(org.elasticsearch.action.search.SearchResponse response) {
        SearchHit[] hits = response.getHits().getHits();
        List<T> items = new ArrayList<>(hits.length);
        for (SearchHit hit : hits) {
            items.add(reader.fromJSON(BytesReference.toBytes(hit.getSourceRef())));
        }
        Aggregations aggregationResponse = response.getAggregations();
        Map<String, Aggregation> aggregations = aggregationResponse == null ? Maps.newHashMap() : aggregationResponse.asMap();
        return new SearchResponse<>(items, response.getHits().getTotalHits(), aggregations);
    }

    @Override
    public List<String> complete(CompleteRequest request) {
        StopWatch watch = new StopWatch();
        long esTookTime = 0;
        String index = request.index == null ? this.index : request.index;
        int options = 0;
        try {
            SuggestBuilder suggest = new SuggestBuilder().setGlobalText(request.prefix);
            for (String field : request.fields) {
                CompletionSuggestionBuilder suggestion = SuggestBuilders.completionSuggestion(field).skipDuplicates(true);
                if (request.limit != null) suggestion.size(request.limit);
                suggest.addSuggestion("completion:" + field, suggestion);
            }
            SearchRequestBuilder builder = client().prepareSearch(index).setFetchSource(false).suggest(suggest);
            logger.debug("complete, index={}, type={}, request={}", index, type, builder);

            org.elasticsearch.action.search.SearchResponse response = builder.get();
            esTookTime = response.getTook().nanos();
            if (response.getFailedShards() > 0) logger.warn("some shard failed, response={}", response);

            Set<String> suggestions = response.getSuggest().filter(CompletionSuggestion.class).stream()
                                              .map(CompletionSuggestion::getOptions).flatMap(Collection::stream).map(option -> option.getText().string())
                                              .collect(Collectors.toCollection(LinkedHashSet::new));
            options = suggestions.size();
            return new ArrayList<>(suggestions);
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime, options, 0);
            logger.debug("complete, options={}, esTookTime={}, elapsedTime={}", options, esTookTime, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public Optional<T> get(GetRequest request) {
        StopWatch watch = new StopWatch();
        String index = request.index == null ? this.index : request.index;
        int hits = 0;
        try {
            GetResponse response = client().prepareGet(index, type, request.id).get();
            if (!response.isExists()) return Optional.empty();
            hits = 1;
            return Optional.of(reader.fromJSON(response.getSourceAsBytes()));
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime, hits, 0);
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
            client().prepareIndex(index, type, request.id).setSource(document, XContentType.JSON).get();
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime, 0, 1);
            logger.debug("index, index={}, type={}, id={}, elapsedTime={}", index, type, request.id, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public void bulkIndex(BulkIndexRequest<T> request) {
        if (request.sources == null || request.sources.isEmpty()) throw Exceptions.error("request.sources must not be empty");

        StopWatch watch = new StopWatch();
        String index = request.index == null ? this.index : request.index;
        BulkRequestBuilder builder = client().prepareBulk();
        for (Map.Entry<String, T> entry : request.sources.entrySet()) {
            String id = entry.getKey();
            T source = entry.getValue();
            validator.validate(source);
            byte[] document = writer.toJSON(source);
            builder.add(client().prepareIndex(index, type, id).setSource(document, XContentType.JSON));
        }
        long esTookTime = 0;
        try {
            BulkResponse response = builder.get();
            esTookTime = response.getTook().nanos();
            if (response.hasFailures()) throw new SearchException(response.buildFailureMessage());
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime, 0, request.sources.size());
            logger.debug("bulkIndex, index={}, type={}, size={}, esTookTime={}, elapsedTime={}", index, type, request.sources.size(), esTookTime, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public boolean delete(DeleteRequest request) {
        StopWatch watch = new StopWatch();
        String index = request.index == null ? this.index : request.index;
        boolean deleted = false;
        try {
            DeleteResponse response = client().prepareDelete(index, type, request.id).get();
            deleted = response.getResult() == DocWriteResponse.Result.DELETED;
            return deleted;
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime, 0, deleted ? 1 : 0);
            logger.debug("delete, index={}, type={}, id={}, elapsedTime={}", index, type, request.id, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public void bulkDelete(BulkDeleteRequest request) {
        if (request.ids == null || request.ids.isEmpty()) throw Exceptions.error("request.ids must not be empty");

        StopWatch watch = new StopWatch();
        String index = request.index == null ? this.index : request.index;
        BulkRequestBuilder builder = client().prepareBulk();
        for (String id : request.ids) {
            builder.add(client().prepareDelete(index, type, id));
        }
        long esTookTime = 0;
        try {
            BulkResponse response = builder.get();
            esTookTime = response.getTook().nanos();
            if (response.hasFailures()) throw new SearchException(response.buildFailureMessage());
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime, 0, request.ids.size());
            logger.debug("bulkDelete, index={}, type={}, size={}, esTookTime={}, elapsedTime={}", index, type, request.ids.size(), esTookTime, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public long deleteByQuery(DeleteByQueryRequest request) {
        if (request.query == null) throw Exceptions.error("request.query must not be null");

        StopWatch watch = new StopWatch();
        String index = request.index == null ? this.index : request.index;
        long esTookTime = 0;
        long deleted = 0;
        try {
            DeleteByQueryRequestBuilder builder = DeleteByQueryAction.INSTANCE.newRequestBuilder(client());
            BulkByScrollResponse response = builder.filter(request.query)
                                                   .source(index)
                                                   .get();
            esTookTime = response.getTook().nanos();
            deleted = response.getDeleted();
            return deleted;
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime, 0, (int) deleted);
            logger.debug("deleteByQuery, index={}, type={}, deleted={}, esTookTime={}, elapsedTime={}", index, type, deleted, esTookTime, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public List<String> analyze(AnalyzeRequest request) {
        StopWatch watch = new StopWatch();
        String index = request.index == null ? this.index : request.index;
        try {
            AnalyzeResponse response = client().admin().indices().prepareAnalyze(index, request.text).setAnalyzer(request.analyzer).get();
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
        TimeValue keepAlive = TimeValue.timeValueNanos(forEach.scrollTimeout.toNanos());
        long esTookTime = 0;
        String index = forEach.index == null ? this.index : forEach.index;
        int totalHits = 0;
        try {
            SearchRequestBuilder builder = client().prepareSearch(index)
                                                   .setQuery(forEach.query)
                                                   .addSort(SortBuilders.fieldSort("_doc"))
                                                   .setScroll(keepAlive)
                                                   .setSize(forEach.limit);
            logger.debug("foreach, index={}, type={}, request={}", index, type, builder);
            org.elasticsearch.action.search.SearchResponse searchResponse = builder.get();

            while (true) {
                esTookTime += searchResponse.getTook().nanos();
                if (searchResponse.getFailedShards() > 0) logger.warn("some shard failed, response={}", searchResponse);

                SearchHit[] hits = searchResponse.getHits().getHits();
                if (hits.length == 0) break;
                totalHits += hits.length;

                for (SearchHit hit : hits) {
                    forEach.consumer.accept(reader.fromJSON(BytesReference.toBytes(hit.getSourceRef())));
                }

                String scrollId = searchResponse.getScrollId();
                searchResponse = client().prepareSearchScroll(scrollId).setScroll(keepAlive).get();
            }
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("elasticsearch", elapsedTime, totalHits, 0);
            logger.debug("forEach, totalHits={}, esTookTime={}, elapsedTime={}", totalHits, esTookTime, elapsedTime);
        }
    }

    private Client client() {
        return elasticSearch.client();
    }

    private void checkSlowOperation(long elapsedTime) {
        if (elapsedTime > slowOperationThresholdInNanos) {
            logger.warn(Markers.errorCode("SLOW_ES"), "slow elasticsearch operation, elapsedTime={}", elapsedTime);
        }
    }
}
