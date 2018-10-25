package core.framework.search.module;

import core.framework.impl.module.Config;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.module.ShutdownHook;
import core.framework.search.ElasticSearch;
import core.framework.search.ElasticSearchType;
import core.framework.search.impl.ElasticSearchImpl;
import core.framework.search.impl.log.ESLoggerContextFactory;
import core.framework.util.Types;

import java.time.Duration;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class SearchConfig extends Config {
    ElasticSearchImpl search;
    private ModuleContext context;
    private boolean typeAdded;

    @Override
    protected void initialize(ModuleContext context, String name) {
        if (name != null) throw new Error(format("search does not support multiple instances, name={}", name));
        this.context = context;
        configureLogger();
        search = createElasticSearch(context);
        context.beanFactory.bind(ElasticSearch.class, null, search);
    }

    @Override
    protected void validate() {
        if (search.host == null) throw new Error("search host must be configured");
        if (!typeAdded)
            throw new Error("elasticsearch is configured but no type added, please remove unnecessary config");
    }

    public void host(String host) {
        search.host = host;
    }

    private ElasticSearchImpl createElasticSearch(ModuleContext context) {
        var search = new ElasticSearchImpl();
        context.startupHook.add(search::initialize);
        context.shutdownHook.add(ShutdownHook.STAGE_7, timeout -> search.close());
        return search;
    }

    void configureLogger() {
        System.setProperty("log4j2.loggerContextFactory", ESLoggerContextFactory.class.getName());
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
        typeAdded = true;
    }
}
