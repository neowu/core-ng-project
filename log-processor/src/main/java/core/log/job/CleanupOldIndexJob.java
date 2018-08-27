package core.log.job;

import core.framework.inject.Inject;
import core.framework.scheduler.Job;
import core.framework.search.ClusterStateResponse;
import core.framework.search.ElasticSearch;
import core.log.service.IndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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
        ClusterStateResponse state = elasticSearch.state();
        for (var entry : state.metadata.indices.entrySet()) {
            String index = entry.getKey();
            indexService.createdDate(index).ifPresent(date -> {
                long days = ChronoUnit.DAYS.between(date, now);
                if (days >= 30) {        // delete log older than 30 days, close index older than 7 days
                    deleteIndex(index);
                } else if (days >= 7 && entry.getValue().state == ClusterStateResponse.IndexState.OPEN) {
                    closeIndex(index);
                }
            });
        }
    }

    private void deleteIndex(String index) {
        try {
            elasticSearch.deleteIndex(index);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void closeIndex(String index) {
        try {
            elasticSearch.closeIndex(index);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
