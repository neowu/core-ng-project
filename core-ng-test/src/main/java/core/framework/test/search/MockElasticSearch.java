package core.framework.test.search;

import core.framework.api.util.StopWatch;
import core.framework.impl.search.ElasticSearchImpl;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
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
            Settings settings = Settings.settingsBuilder()
                .put("node.local", "true")          // refer to org.elasticsearch.node.NodeBuilder.local()
                .put("http.enabled", "false")       // refer to org.elasticsearch.node.Node.start()
                .put("path.home", dataPath)         // refer to org.elasticsearch.env.Environment.Environment()
                .build();
            Node node = new Node(settings);
            node.start();
            return node.client();
        } finally {
            logger.info("create local elasticsearch node, dataPath={}, elapsedTime={}", dataPath, watch.elapsedTime());
        }
    }
}
