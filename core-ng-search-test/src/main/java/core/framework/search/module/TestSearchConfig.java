package core.framework.search.module;

import core.framework.impl.module.ModuleContext;
import core.framework.search.impl.ESLoggerConfigFactory;
import core.framework.search.impl.ElasticSearchImpl;
import core.framework.search.impl.MockElasticSearch;
import core.framework.util.Files;

import java.nio.file.Path;

/**
 * @author neo
 */
public class TestSearchConfig extends SearchConfig {
    @Override
    void setHost(String host) {
        // set host will parse actual host name by InetSocketAddress, which can not be resolved in test env
    }

    @Override
    ElasticSearchImpl createElasticSearch(ModuleContext context) {
        bindESLogger();
        Path dataPath = Files.tempDir();
        MockElasticSearch search = new MockElasticSearch(dataPath);
        context.shutdownHook.add(search::close);
        context.shutdownHook.add(() -> Files.deleteDir(dataPath));
        return search;
    }

    // es refers to log4j core directly in org.elasticsearch.common.logging.Loggers, this is to bridge es log to coreng logger
    // log4j-to-slf4j works if only transport client is used, but our integration test uses Node.
    // refer to org.elasticsearch.index.IndexModule(), in org.elasticsearch.index.SearchSlowLog(), setLevel calls log4j.core api
    private void bindESLogger() {
        if (System.getProperty("log4j.configurationFactory") != null) return;
        System.setProperty("log4j.configurationFactory", ESLoggerConfigFactory.class.getName());
        System.setProperty("log4j2.disable.jmx", "true");
        ESLoggerConfigFactory.bindLogger();
    }
}
