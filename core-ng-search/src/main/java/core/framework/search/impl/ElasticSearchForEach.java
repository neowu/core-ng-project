package core.framework.search.impl;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch.core.search.ResponseBody;
import core.framework.log.ActionLogContext;
import core.framework.search.ForEach;
import core.framework.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;

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
    private String scrollId;

    public ElasticSearchForEach(ForEach<T> forEach, String index, Class<T> documentClass, ElasticSearchImpl elasticSearch) {
        validate(forEach);
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
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ElasticsearchException e) {
            throw elasticSearch.searchException(e);
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("elasticsearch", elapsed, totalHits, 0);
            LOGGER.debug("forEach, totalHits={}, esServerTook={}, esClientTook={}, elapsed={}", totalHits, esServerTook, esClientTook, elapsed);
        }
    }

    private void validate(ForEach<T> forEach) {
        if (forEach.consumer == null) throw new Error("forEach.consumer must not be null");
        if (forEach.query == null) throw new Error("forEach.query must not be null");
        if (forEach.scrollTimeout == null) throw new Error("forEach.scrollTimeout must not be null");
        if (forEach.batchSize == null || forEach.batchSize <= 0) throw new Error("forEach.batchSize must not be null or less than one");
    }
}
