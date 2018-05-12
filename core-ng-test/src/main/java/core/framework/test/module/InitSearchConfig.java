package core.framework.test.module;

import core.framework.impl.search.ElasticSearchTypeImpl;
import core.framework.module.SearchConfig;
import core.framework.search.ElasticSearchType;
import core.framework.util.ClasspathResources;
import core.framework.util.Types;

/**
 * @author neo
 */
public final class InitSearchConfig {
    private final TestModuleContext context;
    private final SearchConfig config;

    InitSearchConfig(TestModuleContext context) {
        this.context = context;
        config = context.findConfig(SearchConfig.class, null)
                        .orElseThrow(() -> new Error("search() must be configured before initSearch()"));
    }

    public void createIndex(String index, String sourcePath) {
        config.search.createIndex(index, ClasspathResources.text(sourcePath));
    }

    public void createIndexTemplate(String name, String sourcePath) {
        config.search.createIndexTemplate(name, ClasspathResources.text(sourcePath));
    }

    public <T> ElasticSearchTypeImpl<T> type(Class<T> documentClass) {
        return context.beanFactory.bean(Types.generic(ElasticSearchType.class, documentClass), null);
    }

    public void flush(String index) {
        config.search.flush(index);
    }
}
