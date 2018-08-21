package core.log.job;

import core.framework.search.ElasticSearch;
import core.framework.search.ElasticSearchIndex;
import core.log.service.IndexService;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

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
        job.indexService = new IndexService();
    }

    @Test
    void cleanup() {
        var indices = List.of(index("action-2017.10.01", IndexMetaData.State.OPEN),
                index("action-2017.10.30", IndexMetaData.State.CLOSE),
                index("action-2017.11.01", IndexMetaData.State.OPEN));
        when(elasticSearch.indices()).thenReturn(indices);

        job.cleanup(LocalDate.of(2017, 11, 8));

        verify(elasticSearch).deleteIndex("action-2017.10.01");
        verify(elasticSearch).closeIndex("action-2017.11.01");
    }

    private ElasticSearchIndex index(String index, IndexMetaData.State state) {
        ElasticSearchIndex elasticSearchIndex = new ElasticSearchIndex();
        elasticSearchIndex.index = index;
        elasticSearchIndex.state = state;
        return elasticSearchIndex;
    }
}
