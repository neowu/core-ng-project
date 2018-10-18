package core.log.service;

import core.framework.inject.Inject;
import core.framework.internal.log.message.StatMessage;
import core.framework.search.ElasticSearchType;
import core.framework.search.GetRequest;
import core.framework.util.Lists;
import core.log.IntegrationTest;
import core.log.domain.StatDocument;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class StatServiceTest extends IntegrationTest {
    @Inject
    StatService statService;
    @Inject
    IndexService indexService;
    @Inject
    ElasticSearchType<StatDocument> statType;

    @Test
    void index() {
        StatMessage message = message("1");

        var now = LocalDate.of(2017, Month.OCTOBER, 10);
        statService.index(List.of(message), now);

        StatDocument stat = statDocument(now, message.id);
        assertThat(stat.stats).isEqualTo(message.stats);
    }

    @Test
    void bulkIndex() {
        List<StatMessage> messages = Lists.newArrayList();
        for (int i = 0; i < 6; i++) {
            messages.add(message("bulk-" + i));
        }

        var now = LocalDate.of(2017, Month.OCTOBER, 10);
        statService.index(messages, now);

        StatDocument stat = statDocument(now, messages.get(0).id);
        assertThat(stat.stats).isEqualTo(messages.get(0).stats);
    }

    private StatDocument statDocument(LocalDate now, String id) {
        var request = new GetRequest();
        request.index = indexService.indexName("stat", now);
        request.id = id;
        return statType.get(request).orElseThrow(() -> new Error("not found"));
    }

    private StatMessage message(String id) {
        var message = new StatMessage();
        message.id = id;
        message.date = Instant.now();
        message.stats = Map.of("thread_count", 10d);
        return message;
    }
}
