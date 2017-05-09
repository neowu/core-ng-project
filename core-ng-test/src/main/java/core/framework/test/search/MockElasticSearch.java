package core.framework.test.search;

import core.framework.api.util.StopWatch;
import core.framework.impl.search.ElasticSearchImpl;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.network.NetworkModule;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.node.NodeValidationException;
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
            Settings.Builder settings = Settings.builder();
            settings.put(NetworkModule.TRANSPORT_TYPE_SETTING.getKey(), NetworkModule.LOCAL_TRANSPORT)
                    .put(NetworkModule.HTTP_ENABLED.getKey(), false)
                    .put(Environment.PATH_HOME_SETTING.getKey(), dataPath);
            MockNode node = new MockNode(settings.build());
            node.start();
            return node.client();
        } catch (NodeValidationException e) {
            throw new Error(e);
        } finally {
            logger.info("create local elasticsearch node, dataPath={}, elapsedTime={}", dataPath, watch.elapsedTime());
        }
    }
}
