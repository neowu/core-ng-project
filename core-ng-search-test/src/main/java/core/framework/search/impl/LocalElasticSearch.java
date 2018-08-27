package core.framework.search.impl;

import core.framework.util.Files;
import core.framework.util.StopWatch;
import org.elasticsearch.common.network.NetworkService;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.discovery.DiscoveryModule;
import org.elasticsearch.env.Environment;
import org.elasticsearch.node.NodeValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author neo
 */
public class LocalElasticSearch {
    static {
        System.setProperty("es.scripting.exception_for_missing_value", "true"); // refer to org.elasticsearch.script.ScriptModule
    }

    private final Logger logger = LoggerFactory.getLogger(LocalElasticSearch.class);
    private Path dataPath;
    private LocalNode node;

    public void start() {
        var watch = new StopWatch();
        this.dataPath = Files.tempDir();
        try {
            Settings.Builder settings = Settings.builder();
            settings.put(Environment.PATH_HOME_SETTING.getKey(), dataPath)
                    .put(NetworkService.GLOBAL_NETWORK_BINDHOST_SETTING.getKey(), "_local_")
                    .put(DiscoveryModule.DISCOVERY_TYPE_SETTING.getKey(), "single-node");
            node = new LocalNode(settings.build());
            node.start();
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
