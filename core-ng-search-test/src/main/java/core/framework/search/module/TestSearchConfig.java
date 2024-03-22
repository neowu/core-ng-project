package core.framework.search.module;

import core.framework.internal.module.ModuleContext;
import core.framework.internal.module.ShutdownHook;
import core.framework.search.impl.ESLoggerConfigFactory;
import core.framework.search.impl.LocalElasticSearch;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.elasticsearch.common.logging.internal.LoggerFactoryImpl;
import org.elasticsearch.logging.internal.spi.LoggerFactory;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author neo
 */
public class TestSearchConfig extends SearchConfig {
    private static final ReentrantLock LOCK = new ReentrantLock();

    // only start one local node for testing to reduce resource overhead,
    // only breaking case is that multiple search() using same index name, then if one unit test operates both ElasticSearchType will result in conflict or merged results
    // this can be avoided by designing test differently
    private static HttpHost localESHost;

    @Override
    protected void initialize(ModuleContext context, String name) {
        super.initialize(context, name);
        configureLogger();
        startLocalElasticSearch(context);
    }

    private void startLocalElasticSearch(ModuleContext context) {
        LOCK.lock();
        try {
            // in test env, config is initialized in order and within same thread, so no threading issue
            if (localESHost == null) {
                var server = new LocalElasticSearch();
                localESHost = server.start();
                context.shutdownHook.add(ShutdownHook.STAGE_6, timeout -> server.close());
            }
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public void host(String host) {
        search.hosts = new HttpHost[]{localESHost};
    }

    @Override
    public void auth(String apiKeyId, String apiKeySecret) {

    }

    // ES uses log4j2 core api directly, cannot use log4j-to-slf4j to bridge, refer to following exception
    // here is to bridge to core-ng logger
    /*
      java.lang.ClassCastException: class org.apache.logging.slf4j.SLF4JLoggerContext cannot be cast to class org.apache.logging.log4j.core.LoggerContext (org.apache.logging.slf4j.SLF4JLoggerContext and org.apache.logging.log4j.core.LoggerContext are in unnamed module of loader 'app')
        at org.apache.logging.log4j.core.LoggerContext.getContext(LoggerContext.java:231)
        at org.apache.logging.log4j.core.config.Configurator.setLevel(Configurator.java:366)
        at org.elasticsearch.common.logging.Loggers.setLevel(Loggers.java:114)
        at org.elasticsearch.index.SearchSlowLog.<init>(SearchSlowLog.java:111)
        at org.elasticsearch.index.IndexModule.<init>(IndexModule.java:176)
    * */
    void configureLogger() {
        // ES starts to migrate log4j api to its own logger api, once important info migrated (like node info), we will remove log4j adapter
        if (LoggerFactory.provider() == null) LoggerFactory.setInstance(new LoggerFactoryImpl());

        if (System.getProperty(ConfigurationFactory.CONFIGURATION_FACTORY_PROPERTY) != null) return;
        System.setProperty(ConfigurationFactory.CONFIGURATION_FACTORY_PROPERTY, ESLoggerConfigFactory.class.getName());
        // refer to org.apache.logging.log4j.core.jmx.Server.PROPERTY_DISABLE_JMX, disable to reduce overhead
        System.setProperty("log4j2.disable.jmx", "true");
        ESLoggerConfigFactory.configureLogger();
    }
}
