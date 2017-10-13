package core.log.service;

import core.framework.impl.log.queue.StatMessage;
import core.framework.search.ElasticSearchType;
import core.framework.search.GetRequest;
import core.framework.util.Lists;
import core.framework.util.Maps;
import core.log.IntegrationTest;
import core.log.domain.StatDocument;
import org.junit.Test;

import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class StatServiceTest extends IntegrationTest {
    @Inject
    StatService statService;

    @Inject
    ElasticSearchType<StatDocument> statType;

    @Test
    public void index() {
        StatMessage message = new StatMessage();
        message.id = "1";
        message.date = Instant.now();
        message.stats = Maps.newHashMap("thread_count", 10d);

        LocalDate now = LocalDate.of(2017, Month.OCTOBER, 10);
        statService.index(Lists.newArrayList(message), now);

        GetRequest request = new GetRequest();
        request.index = IndexName.name("stat", now);
        request.id = message.id;
        StatDocument stat = statType.get(request).orElseThrow(() -> new Error("not found"));
        assertEquals(message.stats, stat.stats);
    }
}
