package core.framework.search.impl;

import core.framework.util.Files;
import core.framework.util.StopWatch;
import org.apache.http.HttpHost;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.common.network.NetworkService;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.concurrent.EsExecutors;
import org.elasticsearch.discovery.DiscoveryModule;
import org.elasticsearch.env.Environment;
import org.elasticsearch.http.HttpServerTransport;
import org.elasticsearch.indices.breaker.HierarchyCircuitBreakerService;
import org.elasticsearch.node.LocalNode;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author neo
 */
public class LocalElasticSearch {
    private final Logger logger = LoggerFactory.getLogger(LocalElasticSearch.class);
    private Path dataPath;
    private LocalNode node;

    public HttpHost start() {
        var watch = new StopWatch();
        this.dataPath = Files.tempDir();
        try {
            Settings.Builder settings = Settings.builder();
            settings.put(ClusterName.CLUSTER_NAME_SETTING.getKey(), "test")
                .put(Node.NODE_NAME_SETTING.getKey(), "test")
                .put(Environment.PATH_HOME_SETTING.getKey(), dataPath)
                .put(NetworkService.GLOBAL_NETWORK_BIND_HOST_SETTING.getKey(), NetworkService.DEFAULT_NETWORK_HOST)
                .put(DiscoveryModule.DISCOVERY_TYPE_SETTING.getKey(), DiscoveryModule.SINGLE_NODE_DISCOVERY_TYPE)
                .put(EsExecutors.NODE_PROCESSORS_SETTING.getKey(), 1)
                .put(HierarchyCircuitBreakerService.USE_REAL_MEMORY_USAGE_SETTING.getKey(), false);
            node = new LocalNode(settings.build());
            node.start();
            // on same local server, there may be multiple es started, e.g. multiple test jobs on shared build server, this is to retrieve actual http port
            return new HttpHost("localhost", node.injector().getInstance(HttpServerTransport.class).boundAddress().publishAddress().getPort());
        } catch (NodeValidationException e) {
            throw new Error(e);
        } finally {
            logger.info("create local elasticsearch node, dataPath={}, elapsed={}", dataPath, watch.elapsed());
        }
    }

    public void close() throws IOException {
        if (node == null) return;

        logger.info("close local elasticsearch node");
        node.close();
        Files.deleteDir(dataPath);
    }
}
