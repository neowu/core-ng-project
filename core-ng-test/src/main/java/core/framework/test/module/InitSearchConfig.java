package core.framework.test.module;

import core.framework.api.search.ElasticSearchType;
import core.framework.api.util.ClasspathResources;
import core.framework.api.util.Types;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.search.ElasticSearch;
import core.framework.impl.search.ElasticSearchTypeImpl;

/**
 * @author neo
 */
public final class InitSearchConfig {
    private final ModuleContext context;
    private final ElasticSearch search;

    public InitSearchConfig(ModuleContext context) {
        this.context = context;
        if (!context.beanFactory.registered(ElasticSearch.class, null)) {
            throw new Error("search is not configured, please use search() to configure");
        }

        search = context.beanFactory.bean(ElasticSearch.class, null);
    }

    public void createIndex(String index, String sourcePath) {
        search.createIndex(index, ClasspathResources.text(sourcePath));
    }

    public void createIndexTemplate(String name, String sourcePath) {
        search.createIndexTemplate(name, ClasspathResources.text(sourcePath));
    }

    public <T> ElasticSearchTypeImpl<T> type(Class<T> documentClass) {
        return context.beanFactory.bean(Types.generic(ElasticSearchType.class, documentClass), null);
    }

    public void flush(String index) {
        search.flush(index);
    }
}
