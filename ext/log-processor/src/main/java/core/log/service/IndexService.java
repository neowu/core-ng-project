package core.log.service;

import core.framework.inject.Inject;
import core.framework.search.ElasticSearch;
import core.framework.util.ClasspathResources;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author neo
 */
public class IndexService {
    private final DateTimeFormatter indexDateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd"); // follow same pattern as elastic.co product line, e.g. metricbeats, in order to unify cleanup job
    private final Pattern indexPattern = Pattern.compile("[\\w\\.\\-]+-(\\d{4}\\.\\d{2}\\.\\d{2})");
    @Inject
    ElasticSearch search;
    @Inject
    IndexOption option;

    public void createIndexTemplates() {
        search.putIndexTemplate("action", template("index/action-index-template.json"));
        search.putIndexTemplate("trace", template("index/trace-index-template.json"));
        search.putIndexTemplate("stat", template("index/stat-index-template.json"));
        search.putIndexTemplate("event", template("index/event-index-template.json"));
    }

    public String indexName(String name, LocalDate now) {
        return name + "-" + now.format(indexDateFormatter);
    }

    public Optional<LocalDate> createdDate(String index) {
        Matcher matcher = indexPattern.matcher(index);
        if (!matcher.matches()) return Optional.empty();
        String timestamp = matcher.group(1);
        return Optional.of(LocalDate.parse(timestamp, indexDateFormatter));
    }

    String template(String path) {
        String template = ClasspathResources.text(path);
        return template.replace("${NUMBER_OF_SHARDS}", String.valueOf(option.numberOfShards))
            .replace("${REFRESH_INTERVAL}", option.refreshInterval);
    }
}
