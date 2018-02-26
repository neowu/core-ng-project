package core.framework.module;

import core.framework.impl.module.Config;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.search.ElasticSearchImpl;
import core.framework.impl.search.log.ESLoggerContextFactory;
import core.framework.search.ElasticSearch;
import core.framework.search.ElasticSearchType;
import core.framework.util.Files;
import core.framework.util.Types;

import java.nio.file.Path;
import java.time.Duration;

/**
 * @author neo
 */
public final class SearchConfig {
    private final ModuleContext context;
    private final State state;

    SearchConfig(ModuleContext context) {
        this.context = context;
        state = context.config.state("elasticsearch", State::new);

        if (state.search == null) {
            state.search = createElasticSearch(context);
        }
    }

    private ElasticSearchImpl createElasticSearch(ModuleContext context) {
        ElasticSearchImpl search;
        if (context.isTest()) {
            Path dataPath = Files.tempDir();
            search = context.mockFactory.create(ElasticSearchImpl.class, dataPath);
            context.shutdownHook.add(() -> Files.deleteDir(dataPath));
        } else {
            System.setProperty("log4j2.loggerContextFactory", ESLoggerContextFactory.class.getName());
            search = new ElasticSearchImpl();
            context.startupHook.add(search::initialize);
        }
        context.shutdownHook.add(search::close);
        context.beanFactory.bind(ElasticSearch.class, null, search);
        return search;
    }

    public void host(String host) {
        if (!context.isTest()) {
            state.search.host(host);      // es requires host must be resolved, skip for unit test
        }
        state.host = host;
    }

    public void sniff(boolean sniff) {
        state.search.sniff = sniff;
    }

    public void slowOperationThreshold(Duration threshold) {
        state.search.slowOperationThreshold = threshold;
    }

    public void timeout(Duration timeout) {
        state.search.timeout = timeout;
    }

    public <T> void type(Class<T> documentClass) {
        ElasticSearchType<T> searchType = state.search.type(documentClass);
        context.beanFactory.bind(Types.generic(ElasticSearchType.class, documentClass), null, searchType);
    }

    public static class State implements Config.State {
        public ElasticSearchImpl search;
        String host;

        @Override
        public void validate() {
            if (host == null) throw new Error("search().host() must be configured");
        }
    }
}
