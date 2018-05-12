package core.framework.search.config;

import core.framework.impl.module.ModuleContext;
import core.framework.search.ElasticSearchType;
import core.framework.search.impl.ElasticSearchTypeImpl;
import core.framework.test.module.TestModuleContext;
import core.framework.util.ClasspathResources;
import core.framework.util.Types;

/**
 * @author neo
 */
public final class InitSearchConfig {
    private final TestModuleContext context;
    private final SearchConfig config;

    InitSearchConfig(ModuleContext context) {
        this.context = (TestModuleContext) context;
        config = this.context.findConfig(SearchConfig.class, null)
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
