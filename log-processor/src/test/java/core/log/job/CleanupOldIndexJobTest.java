package core.log.job;

import core.framework.search.ElasticSearch;
import core.framework.search.ElasticSearchIndex;
import core.framework.util.Lists;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class CleanupOldIndexJobTest {
    private CleanupOldIndexJob job;
    private ElasticSearch elasticSearch;

    @BeforeEach
    void createCleanupOldIndexJob() {
        elasticSearch = mock(ElasticSearch.class);

        job = new CleanupOldIndexJob();
        job.elasticSearch = elasticSearch;
    }

    @Test
    void cleanup() {
        List<ElasticSearchIndex> indices = Lists.newArrayList();
        indices.add(index("action-2017-10-01", IndexMetaData.State.OPEN));
        indices.add(index("action-2017-10-30", IndexMetaData.State.CLOSE));
        indices.add(index("action-2017-11-01", IndexMetaData.State.OPEN));
        when(elasticSearch.indices()).thenReturn(indices);

        job.cleanup(LocalDate.of(2017, 11, 8));

        verify(elasticSearch).deleteIndex("action-2017-10-01");
        verify(elasticSearch).closeIndex("action-2017-11-01");
    }

    private ElasticSearchIndex index(String index, IndexMetaData.State state) {
        ElasticSearchIndex elasticSearchIndex = new ElasticSearchIndex();
        elasticSearchIndex.index = index;
        elasticSearchIndex.state = state;
        return elasticSearchIndex;
    }

    @Test
    void createdDate() {
        assertEquals(LocalDate.of(2016, Month.FEBRUARY, 3), job.createdDate("action-2016-02-03").get());
        assertEquals(LocalDate.of(2015, Month.NOVEMBER, 15), job.createdDate("stat-2015-11-15").get());
        assertFalse(job.createdDate(".kibana").isPresent());
    }
}
