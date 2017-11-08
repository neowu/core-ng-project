package core.log.service;

import core.framework.impl.log.message.StatMessage;
import core.framework.inject.Inject;
import core.framework.search.ElasticSearchType;
import core.framework.search.GetRequest;
import core.framework.util.Lists;
import core.framework.util.Maps;
import core.log.IntegrationTest;
import core.log.domain.StatDocument;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class StatServiceTest extends IntegrationTest {
    @Inject
    StatService statService;

    @Inject
    ElasticSearchType<StatDocument> statType;

    @Test
    void index() {
        StatMessage message = message("1");

        LocalDate now = LocalDate.of(2017, Month.OCTOBER, 10);
        statService.index(Lists.newArrayList(message), now);

        StatDocument stat = statDocument(now, message.id);
        assertEquals(message.stats, stat.stats);
    }

    @Test
    void bulkIndex() {
        List<StatMessage> messages = Lists.newArrayList();
        for (int i = 0; i < 6; i++) {
            messages.add(message("bulk-" + i));
        }

        LocalDate now = LocalDate.of(2017, Month.OCTOBER, 10);
        statService.index(messages, now);

        StatDocument stat = statDocument(now, messages.get(0).id);
        assertEquals(messages.get(0).stats, stat.stats);
    }

    private StatDocument statDocument(LocalDate now, String id) {
        GetRequest request = new GetRequest();
        request.index = IndexName.name("stat", now);
        request.id = id;
        return statType.get(request).orElseThrow(() -> new Error("not found"));
    }

    private StatMessage message(String id) {
        StatMessage message = new StatMessage();
        message.id = id;
        message.date = Instant.now();
        message.stats = Maps.newHashMap("thread_count", 10d);
        return message;
    }
}
