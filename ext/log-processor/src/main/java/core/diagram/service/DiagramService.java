package core.diagram.service;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import core.framework.inject.Inject;
import core.framework.search.ElasticSearchType;
import core.framework.search.SearchRequest;
import core.framework.search.SearchResponse;
import core.framework.search.query.Queries;
import core.framework.web.exception.NotFoundException;
import core.log.domain.ActionDocument;

import java.time.ZonedDateTime;
import java.util.List;

import static core.framework.search.query.Queries.range;
import static core.framework.search.query.Queries.terms;

/**
 * @author neo
 */
public class DiagramService {
    @Inject
    ElasticSearchType<ActionDocument> actionType;

    public String arch(int hours, List<String> includeApps, List<String> excludeApps) {
        var request = new SearchRequest();
        request.index = "action-*";
        request.query = new Query.Builder().bool(b -> filterActions(hours, includeApps, excludeApps)).build();
        request.limit = 0;
        request.aggregations.put("app", Aggregation.of(a -> a.terms(t -> t.field("app").size(100))
            .aggregations("action", sub1 -> sub1.terms(t -> t.field("action").size(500))
                .aggregations("client", sub2 -> sub2.terms(t -> t.field("client").size(100))))));
        SearchResponse<ActionDocument> searchResponse = actionType.search(request);

        var diagram = new ArchDiagram();
        diagram.load(searchResponse);
        return diagram.dot();
    }

    private BoolQuery.Builder filterActions(int hours, List<String> includeApps, List<String> excludeApps) {
        var query = QueryBuilders.bool().must(range("@timestamp", ZonedDateTime.now().minusHours(hours), null));
        if (!includeApps.isEmpty()) {
            query.must(b -> b.bool(q -> q.should(terms("app", includeApps))
                .should(terms("client", includeApps))));
        } else if (!excludeApps.isEmpty()) {
            query.mustNot(terms("app", excludeApps))
                .mustNot(terms("client", excludeApps));
        }
        return query;
    }

    public String action(String actionId) {
        var diagram = new ActionDiagram();
        ActionDocument action = getActionById(actionId);
        List<ActionDocument> actions = findActionByCorrelationIds(action.correlationIds);
        for (ActionDocument document : actions) {
            diagram.add(document);
        }
        return diagram.dot();
    }

    // due to action index sharded by date, to get by id, it has to use search, rather than get by id from specific index
    private ActionDocument getActionById(String id) {
        var request = new SearchRequest();
        request.index = "action-*";
        request.query = Queries.ids(List.of(id));
        List<ActionDocument> documents = actionType.search(request).hits;
        if (documents.isEmpty()) throw new NotFoundException("action not found, id=" + id);
        return documents.getFirst();
    }

    private List<ActionDocument> findActionByCorrelationIds(List<String> correlationIds) {
        var request = new SearchRequest();
        request.index = "action-*";
        request.query = terms("correlation_id", correlationIds);
        request.limit = 10000;
        return actionType.search(request).hits;
    }
}
