package core.log.job;

import core.framework.inject.Inject;
import core.framework.scheduler.Job;
import core.framework.scheduler.JobContext;
import core.framework.search.ClusterStateResponse;
import core.framework.search.ElasticSearch;
import core.log.service.IndexService;
import core.log.service.JobConfig;
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
    @Inject
    JobConfig jobConfig;

    @Override
    public void execute(JobContext context) {
        cleanup(context.scheduledTime.toLocalDate());
    }

    void cleanup(LocalDate now) {
        ClusterStateResponse state = elasticSearch.state();
        for (var entry : state.metadata.indices.entrySet()) {
            String index = entry.getKey();
            indexService.createdDate(index).ifPresent(date -> {
                long days = ChronoUnit.DAYS.between(date, now);
                if (days >= jobConfig.indexRetentionDays) {        // delete index older than indexAliveDays, default is 30
                    deleteIndex(index);
                } else if (days >= jobConfig.indexOpenDays && entry.getValue().state == ClusterStateResponse.IndexState.OPEN) {  // close index older than indexOpenDays, default is 7
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
