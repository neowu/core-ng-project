package core.log.service;

import core.framework.inject.Inject;
import core.framework.search.ElasticSearch;
import core.framework.util.ClasspathResources;
import core.framework.util.Threads;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * @author neo
 */
public class IndexService {
    private final Logger logger = LoggerFactory.getLogger(IndexService.class);
    @Inject
    ElasticSearch search;

    public void createIndexTemplatesUntilSuccess() {
        while (true) {
            try {
                createIndexTemplates();
                return;
            } catch (Throwable e) {
                logger.error("failed to create index templates, retry in 10 seconds", e);
                Threads.sleepRoughly(Duration.ofSeconds(10));
            }
        }
    }

    public void createIndexTemplates() {
        search.createIndexTemplate("action", ClasspathResources.text("index/action-index-template.json"));
        search.createIndexTemplate("trace", ClasspathResources.text("index/trace-index-template.json"));
        search.createIndexTemplate("stat", ClasspathResources.text("index/stat-index-template.json"));
    }
}
