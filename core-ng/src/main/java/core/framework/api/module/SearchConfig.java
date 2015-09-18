package core.framework.api.module;

import core.framework.api.search.ElasticSearchType;
import core.framework.api.util.Files;
import core.framework.api.util.Types;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.search.ElasticSearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Duration;

/**
 * @author neo
 */
public class SearchConfig {
    private final Logger logger = LoggerFactory.getLogger(SearchConfig.class);
    private final ModuleContext context;
    private final ElasticSearch search;

    public SearchConfig(ModuleContext context) {
        this.context = context;
        if (context.beanFactory.registered(ElasticSearch.class, null)) {
            search = context.beanFactory.bean(ElasticSearch.class, null);
        } else {
            search = new ElasticSearch();
            context.shutdownHook.add(search::close);
            context.beanFactory.bind(ElasticSearch.class, null, search);
        }
    }

    public void host(String host) {
        if (context.test) {
            logger.info("use temp local index during test");
            Path dataPath = Files.tempDirectory();
            search.local(dataPath);
            context.shutdownHook.add(() -> Files.deleteDirectory(dataPath));
        } else {
            search.remote(host);
        }
    }

    public void slowQueryThreshold(Duration slowQueryThreshold) {
        search.slowQueryThreshold(slowQueryThreshold);
    }

    public void timeout(Duration timeout) {
        search.timeout(timeout);
    }

    public <T> void type(Class<T> documentClass) {
        ElasticSearchType<T> searchType = search.type(documentClass);
        context.beanFactory.bind(Types.generic(ElasticSearchType.class, documentClass), null, searchType);
    }
}
