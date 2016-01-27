package core.log.job;

import com.carrotsearch.hppc.cursors.ObjectObjectCursor;
import core.framework.api.scheduler.Job;
import core.framework.impl.search.ElasticSearch;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.cluster.state.ClusterStateRequest;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.indices.close.CloseIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * @author neo
 */
public class CleanupOldIndexJob implements Job {
    private final Logger logger = LoggerFactory.getLogger(CleanupOldIndexJob.class);
    @Inject
    ElasticSearch elasticSearch;

    @Override
    public void execute() throws Exception {
        LocalDate now = LocalDate.now();

        AdminClient adminClient = elasticSearch.client().admin();
        ClusterStateResponse response = adminClient.cluster().state(new ClusterStateRequest().clear().metaData(true)).get();
        ImmutableOpenMap<String, IndexMetaData> indices = response.getState().getMetaData().indices();

        for (ObjectObjectCursor<String, IndexMetaData> cursor : indices) {
            process(cursor.value, now);
        }
    }

    private void process(IndexMetaData metaData, LocalDate now) {
        String index = metaData.getIndex();
        if (!index.startsWith("action-") && !index.startsWith("trace-")) return;

        int i = index.indexOf('-');
        String postfix = index.substring(i + 1);
        LocalDate date = LocalDate.parse(postfix);
        long days = ChronoUnit.DAYS.between(date, now);
        if (days > 30) {        // delete log older than 30 days, close index older than 7 days
            deleteIndex(index);
        } else if (days > 7 && metaData.getState() == IndexMetaData.State.OPEN) {
            closeIndex(index);
        }
    }

    private void deleteIndex(String index) {
        try {
            logger.info("delete index, index={}", index);
            elasticSearch.client().admin().indices().delete(new DeleteIndexRequest(index)).actionGet();
        } catch (ElasticsearchException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void closeIndex(String index) {
        try {
            logger.info("close index, index={}", index);
            elasticSearch.client().admin().indices().close(new CloseIndexRequest(index)).actionGet();
        } catch (ElasticsearchException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
