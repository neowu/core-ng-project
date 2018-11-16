package core.log.service;

import core.framework.inject.Inject;
import core.framework.internal.log.message.StatMessage;
import core.framework.search.BulkIndexRequest;
import core.framework.search.ElasticSearchType;
import core.framework.search.IndexRequest;
import core.framework.util.Maps;
import core.log.domain.StatDocument;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class StatService {
    @Inject
    IndexService indexService;
    @Inject
    ElasticSearchType<StatDocument> statType;

    public void index(List<StatMessage> messages) {
        index(messages, LocalDate.now());
    }

    public void index(StatMessage message) {
        index(message.id, stat(message), LocalDate.now());
    }

    void index(List<StatMessage> messages, LocalDate now) {
        if (messages.size() <= 5) { // use single index in quiet time
            for (StatMessage message : messages) {
                index(message.id, stat(message), now);
            }
        } else {
            Map<String, StatDocument> stats = Maps.newHashMapWithExpectedSize(messages.size());
            for (StatMessage message : messages) {
                stats.put(message.id, stat(message));
            }
            index(stats, now);
        }
    }

    private void index(Map<String, StatDocument> stats, LocalDate now) {
        BulkIndexRequest<StatDocument> request = new BulkIndexRequest<>();
        request.index = indexService.indexName("stat", now);
        request.sources = stats;
        statType.bulkIndex(request);
    }

    private void index(String id, StatDocument stat, LocalDate now) {
        IndexRequest<StatDocument> request = new IndexRequest<>();
        request.index = indexService.indexName("stat", now);
        request.id = id;
        request.source = stat;
        statType.index(request);
    }

    private StatDocument stat(StatMessage message) {
        var stat = new StatDocument();
        stat.timestamp = message.date;
        stat.app = message.app;
        stat.serverIP = message.serverIP;
        stat.stats = message.stats;
        return stat;
    }
}
