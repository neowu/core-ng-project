package core.log.service;

import core.framework.inject.Inject;
import core.framework.search.ElasticSearch;
import core.framework.util.ClasspathResources;
import core.framework.util.Threads;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author neo
 */
public class IndexService {
    private final Logger logger = LoggerFactory.getLogger(IndexService.class);
    private final DateTimeFormatter indexDateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd"); // follow same pattern as elastic.co product line, e.g. metricbeats, in order to unify cleanup job
    private final Pattern indexPattern = Pattern.compile("[\\w\\.\\-]+-(\\d{4}\\.\\d{2}\\.\\d{2})");
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

    String indexName(String name, LocalDate now) {
        return name + "-" + now.format(indexDateFormatter);
    }

    public Optional<LocalDate> createdDate(String index) {
        Matcher matcher = indexPattern.matcher(index);
        if (!matcher.matches()) return Optional.empty();
        String timestamp = matcher.group(1);
        return Optional.of(LocalDate.parse(timestamp, indexDateFormatter));
    }
}
