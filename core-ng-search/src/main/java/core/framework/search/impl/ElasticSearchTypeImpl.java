package core.framework.search.impl;

import core.framework.api.json.Property;
import core.framework.impl.validate.Validator;
import core.framework.internal.json.JSONMapper;
import core.framework.log.ActionLogContext;
import core.framework.log.Markers;
import core.framework.search.AnalyzeRequest;
import core.framework.search.BulkDeleteRequest;
import core.framework.search.BulkIndexRequest;
import core.framework.search.CompleteRequest;
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
import core.framework.util.StopWatch;
import core.framework.util.Strings;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.elasticsearch.client.Requests.searchRequest;
import static org.elasticsearch.script.Script.DEFAULT_SCRIPT_LANG;
import static org.elasticsearch.script.Script.DEFAULT_SCRIPT_TYPE;

/**
 * @author neo
 */
public final class ElasticSearchTypeImpl<T> implements ElasticSearchType<T> {
    private final Logger logger = LoggerFactory.getLogger(ElasticSearchTypeImpl.class);

    private final ElasticSearchImpl elasticSearch;
    private final String index;
    private final Validator validator;
    private final long slowOperationThresholdInNanos;
    private final JSONMapper<T> mapper;

    ElasticSearchTypeImpl(ElasticSearchImpl elasticSearch, Class<T> documentClass, Duration slowOperationThreshold) {
        this.elasticSearch = elasticSearch;
        this.slowOperationThresholdInNanos = slowOperationThreshold.toNanos();
        this.index = documentClass.getDeclaredAnnotation(Index.class).name();
        validator = new Validator(documentClass, field -> field.getDeclaredAnnotation(Property.class).name());
        mapper = new JSONMapper<>(documentClass);
    }

    @Override
    public SearchResponse<T> search(SearchRequest request) {
        var watch = new StopWatch();
        validate(request);
        long esTook = 0;
        String index = request.index == null ? this.index : request.index;
        long hits = 0;
        try {
            var searchRequest = searchRequest(index);
            if (request.type != null) searchRequest.searchType(request.type);
            SearchSourceBuilder source = searchRequest.source().query(request.query);
            request.aggregations.forEach(source::aggregation);
            request.sorts.forEach(source::sort);
            if (request.skip != null) source.from(request.skip);
            if (request.limit != null) source.size(request.limit);
            logger.debug("search, index={}, request={}", index, searchRequest);
            org.elasticsearch.action.search.SearchResponse response = elasticSearch.client().search(searchRequest, RequestOptions.DEFAULT);
            hits = response.getHits().getTotalHits();
            esTook = response.getTook().nanos();
            if (response.getFailedShards() > 0) logger.warn("some shard failed, response={}", response);
            return searchResponse(response);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
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
            items.add(mapper.fromJSON(BytesReference.toBytes(hit.getSourceRef())));
        }
        Aggregations aggregationResponse = response.getAggregations();
        Map<String, Aggregation> aggregations = aggregationResponse == null ? Map.of() : aggregationResponse.asMap();
        return new SearchResponse<>(items, response.getHits().getTotalHits(), aggregations);
    }

    @Override
    public List<String> complete(CompleteRequest request) {
        var watch = new StopWatch();
        long esTook = 0;
        String index = request.index == null ? this.index : request.index;
        int options = 0;
        try {
            var suggest = new SuggestBuilder().setGlobalText(request.prefix);
            for (String field : request.fields) {
                CompletionSuggestionBuilder suggestion = SuggestBuilders.completionSuggestion(field).skipDuplicates(true);
                if (request.limit != null) suggestion.size(request.limit);
                suggest.addSuggestion("completion:" + field, suggestion);
            }

            var searchRequest = searchRequest(index);
            searchRequest.source().fetchSource(false).suggest(suggest);
            logger.debug("complete, index={}, request={}", index, searchRequest);

            org.elasticsearch.action.search.SearchResponse response = elasticSearch.client().search(searchRequest, RequestOptions.DEFAULT);
            esTook = response.getTook().nanos();
            if (response.getFailedShards() > 0) logger.warn("some shard failed, response={}", response);

            List<String> suggestions = response.getSuggest().filter(CompletionSuggestion.class).stream()
                                               .map(CompletionSuggestion::getOptions).flatMap(Collection::stream).map(option -> option.getText().string())
                                               .distinct()
                                               .collect(Collectors.toList());
            options = suggestions.size();
            return suggestions;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
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
            var getRequest = new org.elasticsearch.action.get.GetRequest(index, this.index, request.id);
            GetResponse response = elasticSearch.client().get(getRequest, RequestOptions.DEFAULT);
            if (!response.isExists()) return Optional.empty();
            hits = 1;
            return Optional.of(mapper.fromJSON(response.getSourceAsBytes()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("elasticsearch", elapsed, hits, 0);
            logger.debug("get, index={}, id={}, elapsed={}", index, request.id, elapsed);
            checkSlowOperation(elapsed);
        }
    }

    @Override
    public void index(IndexRequest<T> request) {
        var watch = new StopWatch();
        String index = request.index == null ? this.index : request.index;
        validator.validate(request.source, false);
        byte[] document = mapper.toJSON(request.source);
        try {
            var indexRequest = new org.elasticsearch.action.index.IndexRequest(index, this.index, request.id).source(document, XContentType.JSON);
            elasticSearch.client().index(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("elasticsearch", elapsed, 0, 1);
            logger.debug("index, index={}, id={}, elapsed={}", index, request.id, elapsed);
            checkSlowOperation(elapsed);
        }
    }

    @Override
    public void bulkIndex(BulkIndexRequest<T> request) {
        var watch = new StopWatch();
        if (request.sources == null || request.sources.isEmpty()) throw new Error("request.sources must not be empty");
        String index = request.index == null ? this.index : request.index;
        var bulkRequest = new BulkRequest();
        for (Map.Entry<String, T> entry : request.sources.entrySet()) {
            String id = entry.getKey();
            T source = entry.getValue();
            validator.validate(source, false);
            var indexRequest = new org.elasticsearch.action.index.IndexRequest(index, this.index, id).source(mapper.toJSON(source), XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        long esTook = 0;
        try {
            BulkResponse response = elasticSearch.client().bulk(bulkRequest, RequestOptions.DEFAULT);
            esTook = response.getTook().nanos();
            if (response.hasFailures()) throw new SearchException(response.buildFailureMessage());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("elasticsearch", elapsed, 0, request.sources.size());
            logger.debug("bulkIndex, index={}, size={}, esTook={}, elapsed={}", index, request.sources.size(), esTook, elapsed);
            checkSlowOperation(elapsed);
        }
    }

    @Override
    public void update(UpdateRequest<T> request) {
        var watch = new StopWatch();
        if (request.script == null) throw new Error("request.script must not be null");
        String index = request.index == null ? this.index : request.index;
        try {
            Map<String, Object> params = request.params == null ? Map.of() : request.params;
            var script = new Script(DEFAULT_SCRIPT_TYPE, DEFAULT_SCRIPT_LANG, request.script, params);
            var updateRequest = new org.elasticsearch.action.update.UpdateRequest(index, this.index, request.id).script(script);
            elasticSearch.client().update(updateRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("elasticsearch", elapsed, 0, 1);
            logger.debug("update, index={}, id={}, script={}, elapsed={}", index, request.id, request.script, elapsed);
            checkSlowOperation(elapsed);
        }
    }

    @Override
    public boolean delete(DeleteRequest request) {
        var watch = new StopWatch();
        String index = request.index == null ? this.index : request.index;
        boolean deleted = false;
        try {
            var deleteRequest = new org.elasticsearch.action.delete.DeleteRequest(index, this.index, request.id);
            DeleteResponse response = elasticSearch.client().delete(deleteRequest, RequestOptions.DEFAULT);
            deleted = response.getResult() == DocWriteResponse.Result.DELETED;
            return deleted;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("elasticsearch", elapsed, 0, deleted ? 1 : 0);
            logger.debug("delete, index={}, id={}, elapsed={}", index, request.id, elapsed);
            checkSlowOperation(elapsed);
        }
    }

    @Override
    public void bulkDelete(BulkDeleteRequest request) {
        var watch = new StopWatch();
        if (request.ids == null || request.ids.isEmpty()) throw new Error("request.ids must not be empty");

        String index = request.index == null ? this.index : request.index;
        var bulkRequest = new BulkRequest();
        for (String id : request.ids) {
            bulkRequest.add(new org.elasticsearch.action.delete.DeleteRequest(index, this.index, id));
        }
        long esTook = 0;
        try {
            BulkResponse response = elasticSearch.client().bulk(bulkRequest, RequestOptions.DEFAULT);
            esTook = response.getTook().nanos();
            if (response.hasFailures()) throw new SearchException(response.buildFailureMessage());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            long elapsed = watch.elapsed();
            int size = request.ids.size();
            ActionLogContext.track("elasticsearch", elapsed, 0, size);
            logger.debug("bulkDelete, index={}, ids={}, size={}, esTook={}, elapsed={}", index, request.ids, size, esTook, elapsed);
            checkSlowOperation(elapsed);
        }
    }

    @Override
    public List<String> analyze(AnalyzeRequest request) {
        var watch = new StopWatch();
        String index = request.index == null ? this.index : request.index;
        try {
            var analyzeRequest = new org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest(index).analyzer(request.analyzer).text(request.text);
            AnalyzeResponse response = elasticSearch.client().indices().analyze(analyzeRequest, RequestOptions.DEFAULT);
            return response.getTokens().stream().map(AnalyzeResponse.AnalyzeToken::getTerm).collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
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
            var searchRequest = searchRequest(index).scroll(keepAlive);
            searchRequest.source().query(forEach.query).sort(SortBuilders.fieldSort("_doc")).size(forEach.limit);
            logger.debug("forEach, index={}, request={}", index, searchRequest);
            org.elasticsearch.action.search.SearchResponse response = elasticSearch.client().search(searchRequest, RequestOptions.DEFAULT);

            while (true) {
                esServerTook += response.getTook().nanos();
                if (response.getFailedShards() > 0) logger.warn("some shard failed, response={}", response);

                SearchHit[] hits = response.getHits().getHits();
                esClientTook += System.nanoTime() - start;
                if (hits.length == 0) break;
                totalHits += hits.length;

                for (SearchHit hit : hits) {
                    forEach.consumer.accept(mapper.fromJSON(BytesReference.toBytes(hit.getSourceRef())));
                }

                start = System.nanoTime();
                response = elasticSearch.client().scroll(Requests.searchScrollRequest(response.getScrollId()).scroll(keepAlive), RequestOptions.DEFAULT);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
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
            throw new Error(Strings.format("result window is too large, skip + limit must be less than or equal to 10000, skip={}, limit={}", request.skip, request.limit));
    }

    private void validate(ForEach<T> forEach) {
        if (forEach.consumer == null) throw new Error("forEach.consumer must not be null");
        if (forEach.query == null) throw new Error("forEach.query must not be null");
        if (forEach.scrollTimeout == null) throw new Error("forEach.scrollTimeout must not be null");
        if (forEach.limit == null || forEach.limit <= 0) throw new Error("forEach.limit must not be null or less than one");
    }

    private void checkSlowOperation(long elapsed) {
        if (elapsed > slowOperationThresholdInNanos) {
            logger.warn(Markers.errorCode("SLOW_ES"), "slow elasticsearch operation, elapsed={}", elapsed);
        }
    }
}
