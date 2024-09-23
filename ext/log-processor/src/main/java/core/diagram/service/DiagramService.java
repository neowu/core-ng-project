package core.diagram.service;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import core.framework.inject.Inject;
import core.framework.search.ElasticSearchType;
import core.framework.search.SearchRequest;
import core.framework.search.SearchResponse;
import core.framework.search.query.Queries;
import core.framework.web.exception.NotFoundException;
import core.log.domain.ActionDocument;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static core.framework.search.query.Queries.ids;
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
        var boolQuery = new BoolQuery.Builder();
        boolQuery.must(s -> s.range(Queries.dateRange("@timestamp", ZonedDateTime.now().minusHours(hours), null)));
        if (!includeApps.isEmpty()) {
            boolQuery.must(b -> b.bool(q -> q.should(s -> s.terms(terms("app", new ArrayList<>(includeApps))))
                .should(s -> s.terms(terms("client", new ArrayList<>(includeApps))))));
        } else if (!excludeApps.isEmpty()) {
            boolQuery.mustNot(b -> b.terms(terms("app", new ArrayList<>(excludeApps))));
            boolQuery.mustNot(b -> b.terms(terms("client", new ArrayList<>(excludeApps))));
        }
        return boolQuery;
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
        request.query = new Query.Builder().ids(ids(List.of(id))).build();
        List<ActionDocument> documents = actionType.search(request).hits;
        if (documents.isEmpty()) throw new NotFoundException("action not found, id=" + id);
        return documents.getFirst();
    }

    private List<ActionDocument> findActionByCorrelationIds(List<String> correlationIds) {
        var request = new SearchRequest();
        request.index = "action-*";
        request.query = new Query.Builder().terms(terms("correlation_id", correlationIds)).build();
        request.limit = 10000;
        return actionType.search(request).hits;
    }
}
