package core.framework.search.module;

import core.framework.internal.module.Config;
import core.framework.internal.module.ModuleContext;
import core.framework.search.ElasticSearchType;
import core.framework.search.impl.ElasticSearchTypeImpl;
import core.framework.test.module.TestModuleContext;
import core.framework.util.ClasspathResources;
import core.framework.util.Types;

/**
 * @author neo
 */
public final class InitSearchConfig extends Config {
    private TestModuleContext context;
    private SearchConfig config;

    @Override
    protected void initialize(ModuleContext context, String name) {
        this.context = (TestModuleContext) context;
        config = this.context.getConfig(SearchConfig.class, null);
        config.search.initialize(); // for es integration test, it usually calls putIndex, so to initialize before for once
    }

    public void putIndex(String index, String sourcePath) {
        config.search.putIndex(index, ClasspathResources.text(sourcePath));
    }

    public void putIndexTemplate(String name, String sourcePath) {
        config.search.putIndexTemplate(name, ClasspathResources.text(sourcePath));
    }

    @SuppressWarnings("unchecked")
    public <T> ElasticSearchTypeImpl<T> type(Class<T> documentClass) {
        return (ElasticSearchTypeImpl<T>) context.beanFactory.bean(Types.generic(ElasticSearchType.class, documentClass), null);
    }

    public void flush(String index) {
        config.search.refreshIndex(index);
    }
}
