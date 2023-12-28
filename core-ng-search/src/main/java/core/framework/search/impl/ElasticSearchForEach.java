package core.framework.search.impl;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch.core.search.ResponseBody;
import core.framework.log.ActionLogContext;
import core.framework.search.ForEach;
import core.framework.util.StopWatch;
import core.framework.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author neo
 */
public class ElasticSearchForEach<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchForEach.class);

    private final ElasticSearchImpl elasticSearch;
    private final Class<T> documentClass;
    private final String index;
    private final ForEach<T> forEach;
    private final Time keepAlive;
    private final Set<String> scrollIds = new HashSet<>();
    private String scrollId;

    public ElasticSearchForEach(ForEach<T> forEach, String index, Class<T> documentClass, ElasticSearchImpl elasticSearch) {
        validate(forEach, elasticSearch.maxResultWindow);
        this.elasticSearch = elasticSearch;
        this.documentClass = documentClass;
        this.index = index;
        this.forEach = forEach;
        keepAlive = Time.of(t -> t.time(forEach.scrollTimeout.toMillis() + "ms"));
    }

    public void process() {
        var watch = new StopWatch();
        long start = System.nanoTime();
        long esClientTook = 0;
        long esServerTook = 0;

        int totalHits = 0;
        try {
            ResponseBody<T> response = elasticSearch.client.search(builder -> builder.index(index)
                .scroll(keepAlive)
                .query(forEach.query)
                .sort(s -> s.field(f -> f.field("_doc")))
                .size(forEach.batchSize), documentClass);
            scrollId = response.scrollId();
            scrollIds.add(scrollId);
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
                response = elasticSearch.client.scroll(builder -> builder.scrollId(scrollId).scroll(keepAlive), documentClass);
                scrollId = response.scrollId();
                scrollIds.add(scrollId);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ElasticsearchException e) {
            throw elasticSearch.searchException(e);
        } finally {
            clearScrolls();
            long elapsed = watch.elapsed();
            ActionLogContext.track("elasticsearch", elapsed, totalHits, 0);
            LOGGER.debug("forEach, totalHits={}, esServerTook={}, esClientTook={}, elapsed={}", totalHits, esServerTook, esClientTook, elapsed);
        }
    }

    // should not throw exception here to hide original exception, it's called in finally block,
    private void clearScrolls() {
        try {
            if (!scrollIds.isEmpty()) {
                elasticSearch.client.clearScroll(builder -> builder.scrollId(new ArrayList<>(scrollIds)));
            }
        } catch (IOException | ElasticsearchException e) {
            LOGGER.warn("failed to clear scrolls, error={}", e.getMessage(), e);
        }
    }

    private void validate(ForEach<T> forEach, int maxBatchSize) {
        if (forEach.consumer == null) throw new Error("forEach.consumer must not be null");
        if (forEach.query == null) throw new Error("forEach.query must not be null");
        if (forEach.scrollTimeout == null) throw new Error("forEach.scrollTimeout must not be null");
        if (forEach.batchSize == null || forEach.batchSize <= 0 || forEach.batchSize > maxBatchSize)
            throw new Error(Strings.format("forEach.batchSize must within (0, {}], batchSize={}", maxBatchSize, forEach.batchSize));
    }
}
