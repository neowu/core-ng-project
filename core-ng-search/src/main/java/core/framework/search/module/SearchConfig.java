package core.framework.search.module;

import core.framework.internal.module.Config;
import core.framework.internal.module.ModuleContext;
import core.framework.internal.module.ShutdownHook;
import core.framework.search.ElasticSearch;
import core.framework.search.ElasticSearchType;
import core.framework.search.impl.ElasticSearchHost;
import core.framework.search.impl.ElasticSearchImpl;
import core.framework.search.impl.log.ESLoggerContextFactory;
import core.framework.util.Types;
import org.apache.logging.log4j.LogManager;

import java.time.Duration;

/**
 * @author neo
 */
public class SearchConfig extends Config {
    ElasticSearchImpl search;
    private ModuleContext context;
    private String name;
    private boolean typeAdded;

    @Override
    protected void initialize(ModuleContext context, String name) {
        this.context = context;
        this.name = name;

        configureLogger();

        search = new ElasticSearchImpl();
        context.startupHook.add(search::initialize);
        context.shutdownHook.add(ShutdownHook.STAGE_7, timeout -> search.close());
        context.beanFactory.bind(ElasticSearch.class, name, search);
    }

    @Override
    protected void validate() {
        if (search.hosts == null) throw new Error("search host must be configured, name=" + name);
        if (!typeAdded)
            throw new Error("elasticsearch is configured but no type added, please remove unnecessary config, name=" + name);
    }

    // comma separated hosts
    public void host(String host) {
        search.hosts = ElasticSearchHost.parse(host);
    }

    void configureLogger() {
        System.setProperty(LogManager.FACTORY_PROPERTY_NAME, ESLoggerContextFactory.class.getName());
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
