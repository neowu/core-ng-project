package core.framework.search.config;

import core.framework.impl.module.ModuleContext;
import core.framework.search.impl.ElasticSearchImpl;
import core.framework.search.test.ESLoggerConfigFactory;
import core.framework.search.test.MockElasticSearch;
import core.framework.util.Files;

import java.nio.file.Path;

/**
 * @author neo
 */
public class TestSearchConfig extends SearchConfig {
    TestSearchConfig(ModuleContext context) {
        super(context);
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

    @Override
    void setHost(String host) {
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
