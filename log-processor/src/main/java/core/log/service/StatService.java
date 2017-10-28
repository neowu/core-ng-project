package core.log.service;

import core.framework.impl.log.queue.StatMessage;
import core.framework.inject.Inject;
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
    ElasticSearchType<StatDocument> statType;

    public void index(List<StatMessage> messages) {
        LocalDate now = LocalDate.now();
        index(messages, now);
    }

    void index(List<StatMessage> messages, LocalDate now) {
        if (messages.size() <= 5) { // use single index in quiet time
            for (StatMessage message : messages) {
                IndexRequest<StatDocument> request = new IndexRequest<>();
                request.index = IndexName.name("stat", now);
                request.id = message.id;
                request.source = stat(message);
                statType.index(request);
            }
        } else {
            Map<String, StatDocument> stats = Maps.newHashMapWithExpectedSize(messages.size());
            for (StatMessage message : messages) {
                stats.put(message.id, stat(message));
            }
            BulkIndexRequest<StatDocument> request = new BulkIndexRequest<>();
            request.index = IndexName.name("stat", now);
            request.sources = stats;
            statType.bulkIndex(request);
        }
    }

    private StatDocument stat(StatMessage message) {
        StatDocument stat = new StatDocument();
        stat.date = message.date;
        stat.app = message.app;
        stat.serverIP = message.serverIP;
        stat.stats = message.stats;
        return stat;
    }
}
