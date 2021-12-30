package core.framework.search.impl;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.ErrorCause;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
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

import static core.framework.log.Markers.errorCode;

/**
 * @author neo
 */
public final class ElasticSearchTypeImpl<T> implements ElasticSearchType<T> {
    private final Logger logger = LoggerFactory.getLogger(ElasticSearchTypeImpl.class);

    private final ElasticSearchImpl elasticSearch;
    private final String index;
    private final long slowOperationThresholdInNanos;
    private final int maxResultWindow;
    private final Validator<T> validator;
    private final Class<T> documentClass;

    ElasticSearchTypeImpl(ElasticSearchImpl elasticSearch, Class<T> documentClass) {
        this.elasticSearch = elasticSearch;
        this.slowOperationThresholdInNanos = elasticSearch.slowOperationThreshold.toNanos();
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
                    .size(request.limit);
                if (request.trackTotalHitsUpTo != null) builder.trackTotalHits(t -> t.count(request.trackTotalHitsUpTo));
                return builder;
            });
            var response = elasticSearch.client.search(searchRequest, documentClass);

            esTook = response.took() * 1_000_000;
            hits = response.hits().hits().size();
            long total = response.hits().total().value();
            List<T> items = response.hits().hits().stream().map(Hit::source).toList();
            return new SearchResponse<>(items, total, response.aggregations());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ElasticsearchException e) {
            throw elasticSearch.exception(e);
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("elasticsearch", elapsed, hits, 0);
            logger.debug("search, hits={}, esTook={}, elapsed={}", hits, esTook, elapsed);
            checkSlowOperation(elapsed);
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
            var response = elasticSearch.client.search(builder ->
                builder.index(index).suggest(suggest).source(s -> s.fetch(Boolean.FALSE)), documentClass);

            esTook = response.took() * 1_000_000;
            List<String> suggestions = response.suggest().values().stream()
                .flatMap(Collection::stream).flatMap(suggestion -> suggestion.options().stream()).map(option -> option.completion().text())
                .distinct()
                .collect(Collectors.toList());
            options = suggestions.size();
            return suggestions;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ElasticsearchException e) {
            throw elasticSearch.exception(e);
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
            GetResponse<T> response = elasticSearch.client.get(builder -> builder.index(index).id(request.id), documentClass);
            if (!response.found()) return Optional.empty();
            hits = 1;
            return Optional.of(response.source());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ElasticsearchException e) {
            throw elasticSearch.exception(e);
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
        try {
            elasticSearch.client.index(builder -> builder.index(index).id(request.id).document(request.source));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ElasticsearchException e) {
            throw elasticSearch.exception(e);
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
        List<BulkOperation> operations = new ArrayList<>(request.sources.size());
        for (Map.Entry<String, T> entry : request.sources.entrySet()) {
            String id = entry.getKey();
            T source = entry.getValue();
            validator.validate(source, false);
            operations.add(BulkOperation.of(builder -> builder.index(i -> i.index(index).id(id).document(source))));
        }
        long esTook = 0;
        try {
            BulkResponse response = elasticSearch.client.bulk(builder -> builder.operations(operations));
            esTook = response.took() * 1_000_000; // mills to nano
            validate(response);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ElasticsearchException e) {
            throw elasticSearch.exception(e);
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
            Map<String, JsonData> params = request.params == null ? Map.of() : request.params.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, value -> JsonData.of(value.getValue())));
            elasticSearch.client.update(builder ->
                builder.index(index).id(request.id).script(s -> s.inline(i -> i.source(request.script).params(params))), documentClass);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ElasticsearchException e) {
            throw elasticSearch.exception(e);
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
            DeleteResponse response = elasticSearch.client.delete(builder -> builder.index(index).id(request.id));
            deleted = response.result() == Result.Deleted;
            return deleted;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ElasticsearchException e) {
            throw elasticSearch.exception(e);
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
        List<BulkOperation> operations = new ArrayList<>(request.ids.size());
        for (String id : request.ids) {
            operations.add(BulkOperation.of(builder -> builder.delete(r -> r.index(index).id(id))));
        }
        long esTook = 0;
        try {
            BulkResponse response = elasticSearch.client.bulk(builder -> builder.operations(operations));

            esTook = response.took() * 1_000_000; // mills to nano
            validate(response);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ElasticsearchException e) {
            throw elasticSearch.exception(e);
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
            AnalyzeResponse response = elasticSearch.client.indices().analyze(builder -> builder.index(index).analyzer(request.analyzer).text(request.text));
            return response.tokens().stream().map(AnalyzeToken::token).toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ElasticsearchException e) {
            throw elasticSearch.exception(e);
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
        Time keepAlive = Time.of(t -> t.time(forEach.scrollTimeout.toNanos() + "nanos"));
        String index = forEach.index == null ? this.index : forEach.index;
        int totalHits = 0;
        try {
            var response = elasticSearch.client.search(builder -> builder.index(index)
                .scroll(keepAlive)
                .query(forEach.query)
                .sort(s -> s.field(f -> f.field("_doc")))
                .size(forEach.limit), documentClass);
            var holder = new ScrollIdHolder();
            holder.scrollId = response.scrollId();
            while (true) {
                esServerTook += response.took() * 1_000_000;

                var hits = response.hits().hits();
                esClientTook += System.nanoTime() - start;
                if (hits.isEmpty()) break;
                totalHits += hits.size();

                for (var hit : hits) {
                    forEach.consumer.accept(hit.source());
                }

                start = System.nanoTime();
                response = elasticSearch.client.scroll(builder -> builder.scrollId(holder.scrollId).scroll(keepAlive), documentClass);
                holder.scrollId = response.scrollId();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ElasticsearchException e) {
            throw elasticSearch.exception(e);
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("elasticsearch", elapsed, totalHits, 0);
            logger.debug("forEach, totalHits={}, esServerTook={}, esClientTook={}, elapsed={}", totalHits, esServerTook, esClientTook, elapsed);
        }
    }

    private void validate(SearchRequest request) {
        int skip = request.skip == null ? 0 : request.skip;
        int limit = request.limit == null ? 0 : request.limit;
        if (skip + limit > maxResultWindow)
            throw new Error(Strings.format("result window is too large, skip + limit must be less than or equal to max result window, skip={}, limit={}, maxResultWindow={}", request.skip, request.limit, maxResultWindow));
    }

    private void validate(ForEach<T> forEach) {
        if (forEach.consumer == null) throw new Error("forEach.consumer must not be null");
        if (forEach.query == null) throw new Error("forEach.query must not be null");
        if (forEach.scrollTimeout == null) throw new Error("forEach.scrollTimeout must not be null");
        if (forEach.limit == null || forEach.limit <= 0) throw new Error("forEach.limit must not be null or less than one");
    }

    private void validate(BulkResponse response) {
        if (!response.errors()) return;

        var builder = new CodeBuilder();
        builder.append("bulk operation failed, errors=[\n");
        for (BulkResponseItem item : response.items()) {
            ErrorCause error = item.error();
            if (error != null) {
                builder.append("id={}, error={}, stackTrace={}\n", item.id(), error.reason(), error.stackTrace());
            }
        }
        builder.append("]");
        throw new SearchException(builder.build());
    }

    private void checkSlowOperation(long elapsed) {
        if (elapsed > slowOperationThresholdInNanos) {
            logger.warn(errorCode("SLOW_ES"), "slow elasticsearch operation, elapsed={}", Duration.ofNanos(elapsed));
        }
    }

    private static class ScrollIdHolder {
        String scrollId;
    }
}
