package core.framework.module;

import core.framework.impl.module.ModuleContext;
import core.framework.impl.search.ElasticSearchImpl;
import core.framework.impl.search.log.ESLoggerContextFactory;
import core.framework.search.ElasticSearch;
import core.framework.search.ElasticSearchType;
import core.framework.util.Files;
import core.framework.util.Types;

import java.nio.file.Path;
import java.time.Duration;

/**
 * @author neo
 */
public final class SearchConfig {
    public final ElasticSearchImpl search;
    private final ModuleContext context;
    private String host;

    SearchConfig(ModuleContext context) {
        this.context = context;
        search = createElasticSearch(context);
    }

    void validate() {
        if (host == null) throw new Error("search().host() must be configured");
    }

    private ElasticSearchImpl createElasticSearch(ModuleContext context) {
        ElasticSearchImpl search;
        if (context.isTest()) {
            Path dataPath = Files.tempDir();
            search = context.mockFactory.create(ElasticSearchImpl.class, dataPath);
            context.shutdownHook.add(() -> Files.deleteDir(dataPath));
        } else {
            System.setProperty("log4j2.loggerContextFactory", ESLoggerContextFactory.class.getName());
            search = new ElasticSearchImpl();
            context.startupHook.add(search::initialize);
        }
        context.shutdownHook.add(search::close);
        context.beanFactory.bind(ElasticSearch.class, null, search);
        return search;
    }

    public void host(String host) {
        if (!context.isTest()) {
            search.host(host);      // es requires host must be resolved, skip for unit test
        }
        this.host = host;
    }

    public void sniff(boolean sniff) {
        search.sniff = sniff;
    }

    public void slowOperationThreshold(Duration threshold) {
        search.slowOperationThreshold = threshold;
    }

    public void timeout(Duration timeout) {
        search.timeout = timeout;
    }

    public <T> void type(Class<T> documentClass) {
        ElasticSearchType<T> searchType = search.type(documentClass);
        context.beanFactory.bind(Types.generic(ElasticSearchType.class, documentClass), null, searchType);
    }
}
