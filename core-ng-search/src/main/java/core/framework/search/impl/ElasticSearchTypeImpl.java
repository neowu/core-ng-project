package core.framework.search.impl;

import co.elastic.clients.elasticsearch._types.Conflicts;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.ErrorCause;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch._types.ShardFailure;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.DeleteByQueryResponse;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggestOption;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.Suggester;
import co.elastic.clients.elasticsearch.indices.AnalyzeResponse;
import co.elastic.clients.elasticsearch.indices.analyze.AnalyzeToken;
import co.elastic.clients.json.JsonData;
import core.framework.internal.asm.CodeBuilder;
import core.framework.internal.validate.Validator;
import core.framework.log.ActionLogContext;
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
import core.framework.search.PartialUpdateRequest;
import core.framework.search.SearchException;
import core.framework.search.SearchRequest;
import core.framework.search.SearchResponse;
import core.framework.search.UpdateRequest;
import core.framework.util.StopWatch;
import core.framework.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static core.framework.log.Markers.errorCode;

/**
 * @author neo
 */
public final class ElasticSearchTypeImpl<T> implements ElasticSearchType<T> {
    private final Logger logger = LoggerFactory.getLogger(ElasticSearchTypeImpl.class);

    private final ElasticSearchImpl elasticSearch;
    private final String index;
    private final int maxResultWindow;
    private final Validator<T> validator;
    private final Class<T> documentClass;

    ElasticSearchTypeImpl(ElasticSearchImpl elasticSearch, Class<T> documentClass) {
        this.elasticSearch = elasticSearch;
        this.maxResultWindow = elasticSearch.maxResultWindow;
        this.index = documentClass.getDeclaredAnnotation(Index.class).name();
        this.documentClass = documentClass;
        validator = Validator.of(documentClass);
    }

    @Override
    public SearchResponse<T> search(SearchRequest request) {
        var watch = new StopWatch();
        validate(request);
        long esTook = 0;
        String index = request.index == null ? this.index : request.index;
        int hits = 0;
        try {
            var searchRequest = co.elastic.clients.elasticsearch.core.SearchRequest.of(builder -> {
                builder.index(index).query(request.query).aggregations(request.aggregations).sort(request.sorts)
                    .searchType(request.type)
                    .from(request.skip)
                    .size(request.limit)
                    .timeout(elasticSearch.timeout.toMillis() + "ms");
                if (request.trackTotalHitsUpTo != null) builder.trackTotalHits(t -> t.count(request.trackTotalHitsUpTo));
                return builder;
            });
            var response = elasticSearch.client.search(searchRequest, documentClass);
            validate(response);
            esTook = response.took() * 1_000_000;
            hits = response.hits().hits().size();
            long total = response.hits().total() == null ? 0 : response.hits().total().value();
            List<T> items = new ArrayList<>(hits);
            for (Hit<T> hit : response.hits().hits()) {
                items.add(hit.source());
            }
            return new SearchResponse<>(items, total, response.aggregations());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ElasticsearchException e) {
            throw elasticSearch.searchException(e);
        } finally {
            long elapsed = watch.elapsed();
            logger.debug("search, index={}, hits={}, esTook={}, elapsed={}", index, hits, esTook, elapsed);
            ActionLogContext.track("elasticsearch", elapsed, hits, 0);
        }
    }

    @Override
    public List<String> complete(CompleteRequest request) {
        var watch = new StopWatch();
        long esTook = 0;
        String index = request.index == null ? this.index : request.index;
        int options = 0;
        try {
            var suggest = Suggester.of(builder -> {
                builder.text(request.prefix);
                for (String field : request.fields) {
                    builder.suggesters(field, s -> s.completion(c -> c.field(field).skipDuplicates(Boolean.TRUE).size(request.limit)));
                }
                return builder;
            });
            var response = elasticSearch.client.search(builder -> builder.index(index)
                .suggest(suggest)
                .source(s -> s.fetch(Boolean.FALSE))
                .timeout(elasticSearch.timeout.toMillis() + "ms"), documentClass);
            validate(response);
            esTook = response.took() * 1_000_000;
            List<String> suggestions = response.suggest().values().stream()
                .flatMap(Collection::stream).flatMap(suggestion -> suggestion.completion().options().stream()).map(CompletionSuggestOption::text)
                .distinct()
                .collect(Collectors.toList());
            options = suggestions.size();
            return suggestions;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ElasticsearchException e) {
            throw elasticSearch.searchException(e);
        } finally {
            long elapsed = watch.elapsed();
            logger.debug("complete, index={}, options={}, esTook={}, elapsed={}", index, options, esTook, elapsed);
            ActionLogContext.track("elasticsearch", elapsed, options, 0);
        }
    }

    @Override
    public Optional<T> get(GetRequest request) {
        var watch = new StopWatch();
        String index = request.index == null ? this.index : request.index;
        int hits = 0;
        try {
            GetResponse<T> response = elasticSearch.client.get(builder -> builder.index(index).id(request.id), documentClass);
            if (!response.found()) return Optional.empty();
            hits = 1;
            return Optional.of(response.source());  // if source = null, means it didn't save source in es index, which is unexpected, better break here
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ElasticsearchException e) {
            throw elasticSearch.searchException(e);
        } finally {
            long elapsed = watch.elapsed();
            logger.debug("get, index={}, id={}, elapsed={}", index, request.id, elapsed);
            ActionLogContext.track("elasticsearch", elapsed, hits, 0);
        }
    }

    @Override
    public void index(IndexRequest<T> request) {
        var watch = new StopWatch();
        String index = request.index == null ? this.index : request.index;
        validator.validate(request.source, false);
        try {
            elasticSearch.client.index(builder -> builder.index(index).id(request.id).document(request.source));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ElasticsearchException e) {
            throw elasticSearch.searchException(e);
        } finally {
            long elapsed = watch.elapsed();
            logger.debug("index, index={}, id={}, elapsed={}", index, request.id, elapsed);
            ActionLogContext.track("elasticsearch", elapsed, 0, 1);
        }
    }

    @Override
    public void bulkIndex(BulkIndexRequest<T> request) {
        var watch = new StopWatch();
        if (request.sources == null || request.sources.isEmpty()) throw new Error("request.sources must not be empty");
        String index = request.index == null ? this.index : request.index;
        List<BulkOperation> operations = new ArrayList<>(request.sources.size());
        for (Map.Entry<String, T> entry : request.sources.entrySet()) {
            String id = entry.getKey();
            T source = entry.getValue();
            validator.validate(source, false);
            operations.add(BulkOperation.of(builder -> builder.index(i -> i.index(index).id(id).document(source))));
        }
        long esTook = 0;
        try {
            BulkResponse response = elasticSearch.client.bulk(builder -> builder.operations(operations).refresh(refreshValue(request.refresh)));
            esTook = response.took() * 1_000_000; // mills to nano
            validate(response);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ElasticsearchException e) {
            throw elasticSearch.searchException(e);
        } finally {
            long elapsed = watch.elapsed();
            logger.debug("bulkIndex, index={}, size={}, esTook={}, elapsed={}", index, request.sources.size(), esTook, elapsed);
            ActionLogContext.track("elasticsearch", elapsed, 0, request.sources.size());
        }
    }

    @Override
    public boolean update(UpdateRequest request) {
        var watch = new StopWatch();
        if (request.script == null) throw new Error("request.script must not be null");
        String index = request.index == null ? this.index : request.index;
        boolean updated = false;
        try {
            Map<String, JsonData> params = request.params == null ? Map.of() : request.params.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, value -> JsonData.of(value.getValue())));
            UpdateResponse<T> response = elasticSearch.client.update(builder -> builder.index(index)
                .id(request.id)
                .script(s -> s.inline(i -> i.source(request.script).params(params)))
                .retryOnConflict(request.retryOnConflict), documentClass);
            updated = response.result() == Result.Updated;
            return updated;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ElasticsearchException e) {
            throw elasticSearch.searchException(e);
        } finally {
            long elapsed = watch.elapsed();
            logger.debug("update, index={}, id={}, script={}, updated={}, elapsed={}", index, request.id, request.script, updated, elapsed);
            int writeEntries = updated ? 1 : 0;
            ActionLogContext.track("elasticsearch", elapsed, 0, writeEntries);
        }
    }

    @Override
    public boolean partialUpdate(PartialUpdateRequest<T> request) {
        var watch = new StopWatch();
        validator.validate(request.source, true);
        String index = request.index == null ? this.index : request.index;
        boolean updated = false;
        try {
            UpdateResponse<T> response = elasticSearch.client.update(builder -> builder.index(index)
                .id(request.id)
                .doc(request.source)
                .retryOnConflict(request.retryOnConflict), documentClass);
            updated = response.result() == Result.Updated;
            return updated;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ElasticsearchException e) {
            throw elasticSearch.searchException(e);
        } finally {
            long elapsed = watch.elapsed();
            logger.debug("partialUpdate, index={}, id={}, updated={}, elapsed={}", index, request.id, updated, elapsed);
            int writeEntries = updated ? 1 : 0;
            ActionLogContext.track("elasticsearch", elapsed, 0, writeEntries);
        }
    }

    @Override
    public boolean delete(DeleteRequest request) {
        var watch = new StopWatch();
        String index = request.index == null ? this.index : request.index;
        boolean deleted = false;
        try {
            DeleteResponse response = elasticSearch.client.delete(builder -> builder.index(index).id(request.id));
            deleted = response.result() == Result.Deleted;
            return deleted;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ElasticsearchException e) {
            throw elasticSearch.searchException(e);
        } finally {
            long elapsed = watch.elapsed();
            logger.debug("delete, index={}, id={}, elapsed={}", index, request.id, elapsed);
            int writeEntries = deleted ? 1 : 0;
            ActionLogContext.track("elasticsearch", elapsed, 0, writeEntries);
        }
    }

    @Override
    public void bulkDelete(BulkDeleteRequest request) {
        var watch = new StopWatch();
        if (request.ids == null || request.ids.isEmpty()) throw new Error("request.ids must not be empty");

        String index = request.index == null ? this.index : request.index;
        List<BulkOperation> operations = new ArrayList<>(request.ids.size());
        for (String id : request.ids) {
            operations.add(BulkOperation.of(builder -> builder.delete(r -> r.index(index).id(id))));
        }
        long esTook = 0;
        try {
            BulkResponse response = elasticSearch.client.bulk(builder -> builder.operations(operations).refresh(refreshValue(request.refresh)));

            esTook = response.took() * 1_000_000; // mills to nano
            validate(response);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ElasticsearchException e) {
            throw elasticSearch.searchException(e);
        } finally {
            long elapsed = watch.elapsed();
            int size = request.ids.size();
            logger.debug("bulkDelete, index={}, ids={}, size={}, esTook={}, elapsed={}", index, request.ids, size, esTook, elapsed);
            ActionLogContext.track("elasticsearch", elapsed, 0, size);
        }
    }

    @Override
    public long deleteByQuery(DeleteByQueryRequest request) {
        var watch = new StopWatch();
        String index = request.index == null ? this.index : request.index;
        long esTook = 0;
        long deleted = 0;
        try {
            DeleteByQueryResponse response = elasticSearch.client.deleteByQuery(builder -> builder.index(index)
                .query(request.query)
                .scrollSize(request.batchSize)
                .conflicts(Conflicts.Proceed)
                .maxDocs(request.limits)
                .refresh(request.refresh));
            if (response.deleted() != null) deleted = response.deleted();
            if (response.took() != null) esTook = response.took() * 1_000_000;
            return deleted;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ElasticsearchException e) {
            throw elasticSearch.searchException(e);
        } finally {
            long elapsed = watch.elapsed();
            logger.debug("deleteByQuery, index={}, deleted={}, esTook={}, elapsed={}", index, deleted, esTook, elapsed);
            ActionLogContext.track("elasticsearch", elapsed, 0, (int) deleted);
        }
    }

    @Override
    public List<String> analyze(AnalyzeRequest request) {
        var watch = new StopWatch();
        String index = request.index == null ? this.index : request.index;
        try {
            AnalyzeResponse response = elasticSearch.client.indices().analyze(builder -> builder.index(index).analyzer(request.analyzer).text(request.text));
            return response.tokens().stream().map(AnalyzeToken::token).toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ElasticsearchException e) {
            throw elasticSearch.searchException(e);
        } finally {
            long elapsed = watch.elapsed();
            logger.debug("analyze, index={}, analyzer={}, elapsed={}", index, request.analyzer, elapsed);
            ActionLogContext.track("elasticsearch", elapsed);
        }
    }

    @Override
    public void forEach(ForEach<T> forEach) {
        String index = forEach.index == null ? this.index : forEach.index;
        new ElasticSearchForEach<>(forEach, index, documentClass, elasticSearch).process();
    }

    private void validate(SearchRequest request) {
        int skip = request.skip == null ? 0 : request.skip;
        int limit = request.limit == null ? 0 : request.limit;
        if (skip + limit > maxResultWindow)
            throw new Error(Strings.format("result window is too large, skip + limit must be less than or equal to max result window, skip={}, limit={}, maxResultWindow={}", request.skip, request.limit, maxResultWindow));
    }

    private void validate(co.elastic.clients.elasticsearch.core.SearchResponse<T> response) {
        if (response.shards().failed().intValue() > 0) {
            for (ShardFailure failure : response.shards().failures()) {
                ErrorCause reason = failure.reason();
                logger.warn("shared failed, index={}, node={}, status={}, reason={}, trace={}",
                    failure.index(), failure.node(), failure.status(),
                    reason.reason(), reason.stackTrace());
            }
        }
        if (response.timedOut()) {
            logger.warn(errorCode("SLOW_ES"), "some of elasticsearch shards timed out");
        }
    }

    private void validate(BulkResponse response) {
        if (!response.errors()) return;

        var builder = new CodeBuilder();
        builder.append("bulk operation failed, errors=[\n");
        for (BulkResponseItem item : response.items()) {
            ErrorCause error = item.error();
            if (error != null) {
                builder.append("id={}, error={}, causedBy={}, stackTrace={}\n", item.id(), error.reason(), error.causedBy(), error.stackTrace());
            }
        }
        builder.append("]");
        throw new SearchException(builder.build());
    }

    @Nullable
    private Refresh refreshValue(Boolean value) {
        if (value == null) return null;
        return Boolean.TRUE.equals(value) ? Refresh.True : Refresh.False;
    }
}
