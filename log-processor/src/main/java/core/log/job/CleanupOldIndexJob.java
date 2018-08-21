package core.log.job;

import core.framework.inject.Inject;
import core.framework.scheduler.Job;
import core.framework.search.ElasticSearch;
import core.framework.search.ElasticSearchIndex;
import core.framework.search.SearchException;
import core.log.service.IndexService;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * @author neo
 */
public class CleanupOldIndexJob implements Job {
    private final Logger logger = LoggerFactory.getLogger(CleanupOldIndexJob.class);
    @Inject
    ElasticSearch elasticSearch;
    @Inject
    IndexService indexService;

    @Override
    public void execute() {
        LocalDate now = LocalDate.now();
        cleanup(now);
    }

    void cleanup(LocalDate now) {
        List<ElasticSearchIndex> indices = elasticSearch.indices();
        for (ElasticSearchIndex index : indices) {
            indexService.createdDate(index.index).ifPresent(date -> {
                long days = ChronoUnit.DAYS.between(date, now);
                if (days >= 30) {        // delete log older than 30 days, close index older than 7 days
                    deleteIndex(index.index);
                } else if (days >= 7 && index.state == IndexMetaData.State.OPEN) {
                    closeIndex(index.index);
                }
            });
        }
    }

    private void deleteIndex(String index) {
        try {
            elasticSearch.deleteIndex(index);
        } catch (SearchException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void closeIndex(String index) {
        try {
            elasticSearch.closeIndex(index);
        } catch (SearchException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
