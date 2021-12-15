package core.framework.search.module;

import core.framework.internal.module.ModuleContext;
import core.framework.internal.module.ShutdownHook;
import core.framework.search.impl.ESLoggerConfigFactory;
import core.framework.search.impl.LocalElasticSearch;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.core.config.ConfigurationFactory;

/**
 * @author neo
 */
public class TestSearchConfig extends SearchConfig {
    // only start one local node for testing to reduce resource overhead,
    // only breaking case is that multiple search() using same index name, then if one unit test operates both ElasticSearchType will result in conflict or merged results
    // this can be avoided by designing test differently
    private static HttpHost localESHost;

    static {
        // refer to io.netty.util.NettyRuntime.AvailableProcessorsHolder.setAvailableProcessors
        // refer to org.elasticsearch.transport.netty4.Netty4Utils.setAvailableProcessors
        // netty only allows set available processors once
        System.setProperty("es.set.netty.runtime.available.processors", "false");

        // disable warning from org.elasticsearch.node.Node
        // refer to org.elasticsearch.node.Node deprecationLogger/no-jdk
        System.setProperty("es.bundled_jdk", "true");
    }

    @Override
    protected void initialize(ModuleContext context, String name) {
        super.initialize(context, name);
        configureLogger();
        startLocalElasticSearch(context);
    }

    private void startLocalElasticSearch(ModuleContext context) {
        synchronized (TestSearchConfig.class) {
            // in test env, config is initialized in order and within same thread, so no threading issue
            if (localESHost == null) {
                var server = new LocalElasticSearch();
                localESHost = server.start();
                context.shutdownHook.add(ShutdownHook.STAGE_6, timeout -> server.close());
            }
        }
    }

    @Override
    public void host(String host) {
        search.hosts = new HttpHost[]{localESHost};
    }

    // ES Node refers to log4j core API directly in org.elasticsearch.common.logging.Loggers, this is to bridge es log to coreng logger
    // refer to org.elasticsearch.common.logging.NodeAndClusterIdStateListener, NodeAndClusterIdConverter.setNodeIdAndClusterId(nodeId, clusterUUID);
    void configureLogger() {
        if (System.getProperty(ConfigurationFactory.CONFIGURATION_FACTORY_PROPERTY) != null) return;
        System.setProperty(ConfigurationFactory.CONFIGURATION_FACTORY_PROPERTY, ESLoggerConfigFactory.class.getName());
        // refer to org.apache.logging.log4j.core.jmx.Server.PROPERTY_DISABLE_JMX, disable to reduce overhead
        System.setProperty("log4j2.disable.jmx", "true");
        ESLoggerConfigFactory.configureLogger();
    }
}
