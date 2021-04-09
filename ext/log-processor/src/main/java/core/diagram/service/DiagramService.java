package core.diagram.service;

import core.framework.inject.Inject;
import core.framework.search.ElasticSearchType;
import core.framework.search.SearchRequest;
import core.framework.search.SearchResponse;
import core.log.domain.ActionDocument;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;

import java.time.ZonedDateTime;

/**
 * @author neo
 */
public class DiagramService {
    @Inject
    ElasticSearchType<ActionDocument> actionType;

    public Diagram arch() {
        var request = new SearchRequest();
        request.index = "action-*";
        request.query = QueryBuilders.rangeQuery("@timestamp").gt(ZonedDateTime.now().minusHours(12));
        request.limit = 0;
        request.aggregations.add(new TermsAggregationBuilder("app").field("app").size(50)
                .subAggregation(new TermsAggregationBuilder("action").field("action").size(500)
                        .subAggregation(new TermsAggregationBuilder("client").field("client").size(50))));
        SearchResponse<ActionDocument> searchResponse = actionType.search(request);

        Arch arch = new Arch();
        arch.load(searchResponse);
        return arch.diagram();
    }
}
