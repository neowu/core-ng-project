package core.log.kafka;

import core.framework.inject.Inject;
import core.framework.kafka.BulkMessageHandler;
import core.framework.kafka.Message;
import core.framework.log.message.StatMessage;
import core.framework.search.BulkIndexRequest;
import core.framework.search.ElasticSearchType;
import core.framework.util.Maps;
import core.log.domain.StatDocument;
import core.log.service.IndexService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class StatMessageHandler implements BulkMessageHandler<StatMessage> {
    @Inject
    IndexService indexService;
    @Inject
    ElasticSearchType<StatDocument> statType;

    @Override
    public void handle(List<Message<StatMessage>> messages) {
        index(messages, LocalDate.now());
    }

    void index(List<Message<StatMessage>> messages, LocalDate now) {
        Map<String, StatDocument> stats = Maps.newHashMapWithExpectedSize(messages.size());
        for (Message<StatMessage> message : messages) {
            stats.put(message.value.id, stat(message.value));
        }
        BulkIndexRequest<StatDocument> request = new BulkIndexRequest<>();
        request.index = indexService.indexName("stat", now);
        request.sources = stats;
        statType.bulkIndex(request);
    }

    private StatDocument stat(StatMessage message) {
        var stat = new StatDocument();
        stat.timestamp = message.date;
        stat.app = message.app;
        stat.host = message.host;
        stat.result = message.result;
        stat.errorCode = message.errorCode;
        stat.errorMessage = message.errorMessage;
        stat.stats = message.stats;
        stat.info = message.info;
        return stat;
    }
}
