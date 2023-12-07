package core.log.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class IndexServiceTest {
    private IndexService indexService;

    @BeforeEach
    void createIndexService() {
        indexService = new IndexService();

        var option = new IndexOption();
        option.refreshInterval = "3s";
        option.numberOfShards = 3;
        indexService.option = option;
    }

    @Test
    void indexName() {
        assertThat(indexService.indexName("action", LocalDate.of(2016, Month.JANUARY, 15))).isEqualTo("action-2016.01.15");
        assertThat(indexService.indexName("trace", LocalDate.of(2018, Month.AUGUST, 1))).isEqualTo("trace-2018.08.01");
    }

    @Test
    void createdDate() {
        assertThat(indexService.createdDate("action-2016.02.03")).hasValue(LocalDate.of(2016, Month.FEBRUARY, 3));
        assertThat(indexService.createdDate("stat-2015.11.15")).hasValue(LocalDate.of(2015, Month.NOVEMBER, 15));
        assertThat(indexService.createdDate("metricbeat-6.3.2-2018.08.19")).hasValue(LocalDate.of(2018, Month.AUGUST, 19));
        assertThat(indexService.createdDate(".kibana")).isNotPresent();
    }

    @Test
    void template() {
        assertThat(indexService.template("index/action-index-template.json"))
            .contains("\"number_of_shards\": \"3\"")
            .contains("\"refresh_interval\": \"3s\"");
    }
}
