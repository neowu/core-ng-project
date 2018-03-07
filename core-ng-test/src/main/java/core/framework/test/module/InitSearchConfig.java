package core.framework.test.module;

import core.framework.impl.module.ModuleContext;
import core.framework.impl.search.ElasticSearchTypeImpl;
import core.framework.module.SearchConfig;
import core.framework.search.ElasticSearchType;
import core.framework.util.ClasspathResources;
import core.framework.util.Types;

/**
 * @author neo
 */
public final class InitSearchConfig {
    private final ModuleContext context;
    private final SearchConfig.State state;

    InitSearchConfig(ModuleContext context) {
        this.context = context;
        state = context.config.state("elasticsearch");
    }

    public void createIndex(String index, String sourcePath) {
        state.search.createIndex(index, ClasspathResources.text(sourcePath));
    }

    public void createIndexTemplate(String name, String sourcePath) {
        state.search.createIndexTemplate(name, ClasspathResources.text(sourcePath));
    }

    public <T> ElasticSearchTypeImpl<T> type(Class<T> documentClass) {
        return context.beanFactory.bean(Types.generic(ElasticSearchType.class, documentClass), null);
    }

    public void flush(String index) {
        state.search.flush(index);
    }
}
