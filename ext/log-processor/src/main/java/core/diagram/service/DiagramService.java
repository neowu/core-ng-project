package core.diagram.service;

import core.framework.inject.Inject;
import core.framework.search.ElasticSearchType;
import core.framework.search.SearchRequest;
import core.framework.search.SearchResponse;
import core.log.domain.ActionDocument;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;

import java.time.ZonedDateTime;
import java.util.Set;

/**
 * @author neo
 */
public class DiagramService {
    @Inject
    ElasticSearchType<ActionDocument> actionType;

    public String arch(int hours, Set<String> excludeApps) {
        var request = new SearchRequest();
        request.index = "action-*";
        request.query = QueryBuilders.rangeQuery("@timestamp").gt(ZonedDateTime.now().minusHours(hours));
        request.limit = 0;
        request.aggregations.add(new TermsAggregationBuilder("app").field("app").size(100)
            .subAggregation(new TermsAggregationBuilder("action").field("action").size(500)
                .subAggregation(new TermsAggregationBuilder("client").field("client").size(100))));
        SearchResponse<ActionDocument> searchResponse = actionType.search(request);

        var diagram = new ArchDiagram(excludeApps);
        diagram.load(searchResponse);
        return diagram.dot();
    }
}
