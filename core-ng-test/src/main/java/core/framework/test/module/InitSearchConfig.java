package core.framework.test.module;

import core.framework.impl.module.ModuleContext;
import core.framework.impl.search.ElasticSearchTypeImpl;
import core.framework.search.ElasticSearch;
import core.framework.search.ElasticSearchType;
import core.framework.util.ClasspathResources;
import core.framework.util.Types;

/**
 * @author neo
 */
public final class InitSearchConfig {
    private final ModuleContext context;
    private final ElasticSearch search;

    public InitSearchConfig(ModuleContext context) {
        this.context = context;
        if (context.config.search().search == null) {
            throw new Error("search() is not configured");
        }
        search = context.config.search().search;
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
