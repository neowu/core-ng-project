package core.framework.search.module;

import core.framework.impl.module.ModuleContext;
import core.framework.impl.module.ShutdownHook;
import core.framework.search.impl.ESLoggerConfigFactory;
import core.framework.search.impl.LocalElasticSearch;
import org.apache.http.HttpHost;

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
        search.host = this.host;
    }

    // es refers to log4j core directly in org.elasticsearch.common.logging.Loggers, this is to bridge es log to coreng logger
    // log4j-to-slf4j works if only transport client is used, but our integration test uses Node.
    // refer to org.elasticsearch.index.IndexModule(), in org.elasticsearch.index.SearchSlowLog(), setLevel calls log4j.core api
    @Override
    void configureLogger() {
        if (System.getProperty("log4j.configurationFactory") != null) return;
        System.setProperty("log4j.configurationFactory", ESLoggerConfigFactory.class.getName());
        System.setProperty("log4j2.disable.jmx", "true");
        ESLoggerConfigFactory.configureLogger();
    }
}
