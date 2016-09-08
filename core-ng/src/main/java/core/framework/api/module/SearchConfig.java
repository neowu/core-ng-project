package core.framework.api.module;

import core.framework.api.search.ElasticSearch;
import core.framework.api.search.ElasticSearchType;
import core.framework.api.util.Files;
import core.framework.api.util.Types;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.search.ElasticSearchImpl;

import java.nio.file.Path;
import java.time.Duration;

/**
 * @author neo
 */
public final class SearchConfig {
    private final ModuleContext context;
    private final ElasticSearchImpl search;

    public SearchConfig(ModuleContext context) {
        this.context = context;
        if (context.beanFactory.registered(ElasticSearch.class, null)) {
            search = context.beanFactory.bean(ElasticSearch.class, null);
        } else {
            if (context.isTest()) {
                Path dataPath = Files.tempDir();
                search = context.mockFactory.create(ElasticSearchImpl.class, dataPath);
                search.initialize();    // elasticserch client can be initialized immediately for unit test env
                context.shutdownHook.add(() -> Files.deleteDir(dataPath));
            } else {
                search = new ElasticSearchImpl();
                context.startupHook.add(search::initialize);
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

    public void sniff(boolean sniff) {
        if (!context.isTest()) {
            search.sniff(sniff);
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
