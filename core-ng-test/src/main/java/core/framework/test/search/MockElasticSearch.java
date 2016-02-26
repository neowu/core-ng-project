package core.framework.test.search;

import core.framework.api.util.StopWatch;
import core.framework.impl.search.ElasticSearchImpl;
import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * @author neo
 */
public class MockElasticSearch extends ElasticSearchImpl {
    private final Logger logger = LoggerFactory.getLogger(MockElasticSearch.class);
    private final Path dataPath;

    public MockElasticSearch(Path dataPath) {
        this.dataPath = dataPath;
    }

    @Override
    protected Client createClient() {
        StopWatch watch = new StopWatch();
        try {
            LocalNode node = new LocalNode(dataPath);
            node.start();
            return node.client();
        } finally {
            logger.info("create local elasticsearch node, dataPath={}, elapsedTime={}", dataPath, watch.elapsedTime());
        }
    }
}
