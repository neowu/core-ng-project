package core.framework.api.module;

import core.framework.api.search.ElasticSearchType;
import core.framework.api.util.Files;
import core.framework.api.util.Types;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.search.ElasticSearch;

import java.nio.file.Path;
import java.time.Duration;

/**
 * @author neo
 */
public final class SearchConfig {
    private final ModuleContext context;
    private final ElasticSearch search;

    public SearchConfig(ModuleContext context) {
        this.context = context;
        if (context.beanFactory.registered(ElasticSearch.class, null)) {
            search = context.beanFactory.bean(ElasticSearch.class, null);
        } else {
            if (context.isTest()) {
                Path dataPath = Files.tempDir();
                search = context.mockFactory.create(ElasticSearch.class, dataPath);
                context.shutdownHook.add(() -> Files.deleteDir(dataPath));
            } else {
                search = new ElasticSearch();
            }
            context.shutdownHook.add(search::close);
            context.beanFactory.bind(ElasticSearch.class, null, search);
        }
    }

    public void host(String host) {
        if (!context.isTest()) {
            search.host(host);
        }
    }

    public void slowOperationThreshold(Duration threshold) {
        if (!context.isTest()) {
            search.slowOperationThreshold(threshold);
        }
    }

    public void timeout(Duration timeout) {
        if (!context.isTest()) {
            search.timeout(timeout);
        }
    }

    public <T> void type(Class<T> documentClass) {
        ElasticSearchType<T> searchType = search.type(documentClass);
        context.beanFactory.bind(Types.generic(ElasticSearchType.class, documentClass), null, searchType);
    }
}
