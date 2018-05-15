package core.framework.search.impl;

import core.framework.util.StopWatch;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.network.NetworkModule;
import org.elasticsearch.common.network.NetworkService;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.discovery.DiscoveryModule;
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
    Client createClient() {
        StopWatch watch = new StopWatch();
        try {
            Settings.Builder settings = Settings.builder();
            settings.put(Environment.PATH_HOME_SETTING.getKey(), dataPath)
                    .put(NetworkModule.HTTP_ENABLED.getKey(), false)
                    .put(NetworkService.GLOBAL_NETWORK_BINDHOST_SETTING.getKey(), "_local_")
                    .put(DiscoveryModule.DISCOVERY_TYPE_SETTING.getKey(), "single-node");
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
