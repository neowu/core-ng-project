package core.framework.search.module;

import core.framework.impl.module.ModuleContext;
import core.framework.impl.module.ShutdownHook;
import core.framework.search.impl.ESLoggerConfigFactory;
import core.framework.search.impl.LocalElasticSearch;

/**
 * @author neo
 */
public class TestSearchConfig extends SearchConfig {
    private static LocalElasticSearch server;

    static {
        // refer to io.netty.util.NettyRuntime.AvailableProcessorsHolder.setAvailableProcessors
        // refer to org.elasticsearch.transport.netty4.Netty4Utils.setAvailableProcessors
        // netty only allows set available processors once
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

    @Override
    protected void initialize(ModuleContext context, String name) {
        super.initialize(context, name);
        startLocalElasticSearch(context);
    }

    // currently multiple elasticsearch in one service will share one test local elasticsearch, therefor there might be document type collation (e.g. same document type on both es)
    // at this point this behavior is not causing big problem (can be workaround in unit test), so we keep this simple rather than start multiple local es on different port which is more complex and slow
    private void startLocalElasticSearch(ModuleContext context) {
        synchronized (TestSearchConfig.class) {
            // in test env, config is initialized in order and within same thread, so no threading issue
            if (server == null) {
                server = new LocalElasticSearch();
                server.start();
                context.shutdownHook.add(ShutdownHook.STAGE_7, timeout -> server.close());
            }
        }
    }

    @Override
    public void host(String host) {
        search.host = "localhost";
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
