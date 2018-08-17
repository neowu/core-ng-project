package core.framework.search.impl;

import core.framework.api.json.Property;
import core.framework.impl.json.JSONReader;
import core.framework.impl.json.JSONWriter;
import core.framework.impl.validate.Validator;
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
import core.framework.search.UpdateRequest;
import core.framework.util.Exceptions;
import core.framework.util.Maps;
import core.framework.util.StopWatch;
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
import org.elasticsearch.script.Script;
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
    private final Validator validator;
    private final long slowOperationThresholdInNanos;
    private final JSONReader<T> reader;
    private final JSONWriter<T> writer;

    ElasticSearchTypeImpl(ElasticSearchImpl elasticSearch, Class<T> documentClass, Duration slowOperationThreshold) {
        this.elasticSearch = elasticSearch;
        this.slowOperationThresholdInNanos = slowOperationThreshold.toNanos();
        Index index = documentClass.getDeclaredAnnotation(Index.class);
        this.index = index.index();
        this.type = index.type();
        validator = new Validator(documentClass, field -> field.getDeclaredAnnotation(Property.class).name());
        reader = JSONReader.of(documentClass);
        writer = JSONWriter.of(documentClass);
    }

    @Override
    public SearchResponse<T> search(SearchRequest request) {
        var watch = new StopWatch();
        validate(request);
        long esTook = 0;
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
            esTook = searchResponse.getTook().nanos();
            if (searchResponse.getFailedShards() > 0) logger.warn("some shard failed, response={}", searchResponse);
            return searchResponse(searchResponse);
        } catch (Exception e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("elasticsearch", elapsed, (int) hits, 0);
            logger.debug("search, hits={}, esTook={}, elapsed={}", hits, esTook, elapsed);
            checkSlowOperation(elapsed);
        }
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
        var watch = new StopWatch();
        long esTook = 0;
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
            esTook = response.getTook().nanos();
            if (response.getFailedShards() > 0) logger.warn("some shard failed, response={}", response);

            Set<String> suggestions = response.getSuggest().filter(CompletionSuggestion.class).stream()
                                              .map(CompletionSuggestion::getOptions).flatMap(Collection::stream).map(option -> option.getText().string())
                                              .collect(Collectors.toCollection(LinkedHashSet::new));
            options = suggestions.size();
            return new ArrayList<>(suggestions);
        } catch (Exception e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("elasticsearch", elapsed, options, 0);
            logger.debug("complete, options={}, esTook={}, elapsed={}", options, esTook, elapsed);
            checkSlowOperation(elapsed);
        }
    }

    @Override
    public Optional<T> get(GetRequest request) {
        var watch = new StopWatch();
        String index = request.index == null ? this.index : request.index;
        int hits = 0;
        try {
            GetResponse response = client().prepareGet(index, type, request.id).get();
            if (!response.isExists()) return Optional.empty();
            hits = 1;
            return Optional.of(reader.fromJSON(response.getSourceAsBytes()));
        } catch (Exception e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("elasticsearch", elapsed, hits, 0);
            logger.debug("get, index={}, type={}, id={}, elapsed={}", index, type, request.id, elapsed);
            checkSlowOperation(elapsed);
        }
    }

    @Override
    public void index(IndexRequest<T> request) {
        var watch = new StopWatch();
        String index = request.index == null ? this.index : request.index;
        validator.validate(request.source, false);
        byte[] document = writer.toJSON(request.source);
        try {
            client().prepareIndex(index, type, request.id).setSource(document, XContentType.JSON).get();
        } catch (Exception e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("elasticsearch", elapsed, 0, 1);
            logger.debug("index, index={}, type={}, id={}, elapsed={}", index, type, request.id, elapsed);
            checkSlowOperation(elapsed);
        }
    }

    @Override
    public void bulkIndex(BulkIndexRequest<T> request) {
        var watch = new StopWatch();
        if (request.sources == null || request.sources.isEmpty()) throw Exceptions.error("request.sources must not be empty");

        String index = request.index == null ? this.index : request.index;
        BulkRequestBuilder builder = client().prepareBulk();
        for (Map.Entry<String, T> entry : request.sources.entrySet()) {
            String id = entry.getKey();
            T source = entry.getValue();
            validator.validate(source, false);
            byte[] document = writer.toJSON(source);
            builder.add(client().prepareIndex(index, type, id).setSource(document, XContentType.JSON));
        }
        long esTook = 0;
        try {
            BulkResponse response = builder.get();
            esTook = response.getTook().nanos();
            if (response.hasFailures()) throw new SearchException(response.buildFailureMessage());
        } catch (Exception e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("elasticsearch", elapsed, 0, request.sources.size());
            logger.debug("bulkIndex, index={}, type={}, size={}, esTook={}, elapsed={}", index, type, request.sources.size(), esTook, elapsed);
            checkSlowOperation(elapsed);
        }
    }

    @Override
    public void update(UpdateRequest<T> request) {
        var watch = new StopWatch();
        if (request.script == null) throw Exceptions.error("request.script must not be null");

        String index = request.index == null ? this.index : request.index;
        try {
            client().prepareUpdate(index, type, request.id).setScript(new Script(request.script)).get();
        } catch (Exception e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("elasticsearch", elapsed, 0, 1);
            logger.debug("update, index={}, type={}, id={}, script={}, elapsed={}", index, type, request.id, request.script, elapsed);
            checkSlowOperation(elapsed);
        }
    }

    @Override
    public boolean delete(DeleteRequest request) {
        var watch = new StopWatch();
        String index = request.index == null ? this.index : request.index;
        boolean deleted = false;
        try {
            DeleteResponse response = client().prepareDelete(index, type, request.id).get();
            deleted = response.getResult() == DocWriteResponse.Result.DELETED;
            return deleted;
        } catch (Exception e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("elasticsearch", elapsed, 0, deleted ? 1 : 0);
            logger.debug("delete, index={}, type={}, id={}, elapsed={}", index, type, request.id, elapsed);
            checkSlowOperation(elapsed);
        }
    }

    @Override
    public void bulkDelete(BulkDeleteRequest request) {
        var watch = new StopWatch();
        if (request.ids == null || request.ids.isEmpty()) throw Exceptions.error("request.ids must not be empty");

        String index = request.index == null ? this.index : request.index;
        BulkRequestBuilder builder = client().prepareBulk();
        for (String id : request.ids) {
            builder.add(client().prepareDelete(index, type, id));
        }
        long esTook = 0;
        try {
            BulkResponse response = builder.get();
            esTook = response.getTook().nanos();
            if (response.hasFailures()) throw new SearchException(response.buildFailureMessage());
        } catch (Exception e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsed = watch.elapsed();
            int size = request.ids.size();
            ActionLogContext.track("elasticsearch", elapsed, 0, size);
            logger.debug("bulkDelete, index={}, type={}, ids={}, size={}, esTook={}, elapsed={}", index, type, request.ids, size, esTook, elapsed);
            checkSlowOperation(elapsed);
        }
    }

    @Override
    public long deleteByQuery(DeleteByQueryRequest request) {
        var watch = new StopWatch();
        if (request.query == null) throw Exceptions.error("request.query must not be null");

        String index = request.index == null ? this.index : request.index;
        long esTook = 0;
        long deleted = 0;
        try {
            DeleteByQueryRequestBuilder builder = DeleteByQueryAction.INSTANCE.newRequestBuilder(client());
            BulkByScrollResponse response = builder.filter(request.query)
                                                   .source(index)
                                                   .get();
            esTook = response.getTook().nanos();
            deleted = response.getDeleted();
            return deleted;
        } catch (Exception e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("elasticsearch", elapsed, 0, (int) deleted);
            logger.debug("deleteByQuery, index={}, type={}, query={}, deleted={}, esTook={}, elapsed={}", index, type, request.query, deleted, esTook, elapsed);
            checkSlowOperation(elapsed);
        }
    }

    @Override
    public List<String> analyze(AnalyzeRequest request) {
        var watch = new StopWatch();
        String index = request.index == null ? this.index : request.index;
        try {
            AnalyzeResponse response = client().admin().indices().prepareAnalyze(index, request.text).setAnalyzer(request.analyzer).get();
            return response.getTokens().stream().map(AnalyzeResponse.AnalyzeToken::getTerm).collect(Collectors.toList());
        } catch (Exception e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("elasticsearch", elapsed);
            logger.debug("analyze, index={}, analyzer={}, elapsed={}", index, request.analyzer, elapsed);
            checkSlowOperation(elapsed);
        }
    }

    @Override
    public void forEach(ForEach<T> forEach) {
        var watch = new StopWatch();
        long start = System.nanoTime();
        long esClientTook = 0;
        long esServerTook = 0;
        validate(forEach);
        TimeValue keepAlive = TimeValue.timeValueNanos(forEach.scrollTimeout.toNanos());
        String index = forEach.index == null ? this.index : forEach.index;
        int totalHits = 0;
        try {
            SearchRequestBuilder builder = client().prepareSearch(index)
                                                   .setQuery(forEach.query)
                                                   .addSort(SortBuilders.fieldSort("_doc"))
                                                   .setScroll(keepAlive)
                                                   .setSize(forEach.limit);
            logger.debug("forEach, index={}, type={}, request={}", index, type, builder);
            org.elasticsearch.action.search.SearchResponse searchResponse = builder.get();

            while (true) {
                esServerTook += searchResponse.getTook().nanos();
                if (searchResponse.getFailedShards() > 0) logger.warn("some shard failed, response={}", searchResponse);

                SearchHit[] hits = searchResponse.getHits().getHits();
                esClientTook += System.nanoTime() - start;
                if (hits.length == 0) break;
                totalHits += hits.length;

                for (SearchHit hit : hits) {
                    forEach.consumer.accept(reader.fromJSON(BytesReference.toBytes(hit.getSourceRef())));
                }

                start = System.nanoTime();
                String scrollId = searchResponse.getScrollId();
                searchResponse = client().prepareSearchScroll(scrollId).setScroll(keepAlive).get();
            }
        } catch (Exception e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("elasticsearch", elapsed, totalHits, 0);
            logger.debug("forEach, totalHits={}, esServerTook={}, esClientTook={}, elapsed={}", totalHits, esServerTook, esClientTook, elapsed);
        }
    }

    private void validate(SearchRequest request) {
        int skip = request.skip == null ? 0 : request.skip;
        int limit = request.limit == null ? 0 : request.limit;
        if (skip + limit > 10000)
            throw Exceptions.error("result window is too large, skip + limit must be less than or equal to 10000, skip={}, limit={}", request.skip, request.limit);
    }

    private void validate(ForEach<T> forEach) {
        if (forEach.consumer == null) throw new Error("forEach.consumer must not be null");
        if (forEach.query == null) throw new Error("forEach.query must not be null");
        if (forEach.scrollTimeout == null) throw new Error("forEach.scrollTimeout must not be null");
        if (forEach.limit == null || forEach.limit <= 0) throw new Error("forEach.limit must not be null and greater than 0");
    }

    private Client client() {
        return elasticSearch.client();
    }

    private void checkSlowOperation(long elapsed) {
        if (elapsed > slowOperationThresholdInNanos) {
            logger.warn(Markers.errorCode("SLOW_ES"), "slow elasticsearch operation, elapsed={}", elapsed);
        }
    }
}
