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
    static {
        // refer to io.netty.util.NettyRuntime.AvailableProcessorsHolder.setAvailableProcessors
        // refer to org.elasticsearch.transport.netty4.Netty4Utils.setAvailableProcessors
        // netty only allows set available processors once
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

    private HttpHost host;

    @Override
    protected void initialize(ModuleContext context, String name) {
        super.initialize(context, name);
        startLocalElasticSearch(context);
    }

    private void startLocalElasticSearch(ModuleContext context) {
        var server = new LocalElasticSearch();
        host = server.start();
        context.shutdownHook.add(ShutdownHook.STAGE_7, timeout -> server.close());
    }

    @Override
    public void host(String host) {
        search.hosts = new HttpHost[]{this.host};
    }

    // es refers to log4j core directly in org.elasticsearch.common.logging.Loggers, this is to bridge es log to coreng logger
    // log4j-to-slf4j works only if client is used, but integration test uses Node.
    // refer to org.elasticsearch.common.logging.NodeAndClusterIdStateListener, NodeAndClusterIdConverter.setNodeIdAndClusterId(nodeId, clusterUUID);
    // ES keeps removing log4j-core dependency, now seems only above one left
    @Override
    void configureLogger() {
        if (System.getProperty(ConfigurationFactory.CONFIGURATION_FACTORY_PROPERTY) != null) return;
        System.setProperty(ConfigurationFactory.CONFIGURATION_FACTORY_PROPERTY, ESLoggerConfigFactory.class.getName());
        // refer to org.apache.logging.log4j.core.jmx.Server.PROPERTY_DISABLE_JMX, disable to reduce overhead
        System.setProperty("log4j2.disable.jmx", "true");
        ESLoggerConfigFactory.configureLogger();
    }
}
