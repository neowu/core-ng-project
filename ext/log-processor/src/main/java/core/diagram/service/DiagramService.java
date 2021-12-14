package core.diagram.service;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import core.framework.inject.Inject;
import core.framework.search.ElasticSearchType;
import core.framework.search.SearchRequest;
import core.framework.search.SearchResponse;
import core.framework.web.exception.NotFoundException;
import core.log.domain.ActionDocument;

import java.time.ZonedDateTime;
import java.util.List;
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
        request.query = new Query.Builder().range(r -> r.field("@timestamp").gt(JsonData.of(ZonedDateTime.now().minusHours(hours)))).build();
        request.limit = 0;
        request.aggregations.put("app", Aggregation.of(a -> a.terms(t -> t.field("app").size(100))
                .aggregations("action", sub1 -> sub1.terms(t -> t.field("action").size(500))
                        .aggregations("client", sub2 -> sub2.terms(t -> t.field("client").size(100))))));
        SearchResponse<ActionDocument> searchResponse = actionType.search(request);

        var diagram = new ArchDiagram(excludeApps);
        diagram.load(searchResponse);
        return diagram.dot();
    }

    public String action(String actionId) {
        var diagram = new ActionDiagram();
        ActionDocument action = getActionById(actionId);
        boolean isRootAction = action.correlationIds == null;
        if (isRootAction) diagram.add(action);
        List<String> correlationIds = isRootAction ? List.of(actionId) : action.correlationIds;
        List<ActionDocument> actions = findActionByCorrelationIds(correlationIds);
        actions.forEach(diagram::add);
        if (!isRootAction) {    // if not root action, then correlationId will be id of root action
            List<ActionDocument> rootActions = findActionByIds(correlationIds);
            for (ActionDocument rootAction : rootActions) {
                diagram.add(rootAction);
            }
        }
        return diagram.dot();
    }

    private ActionDocument getActionById(String id) {
        var request = new SearchRequest();
        request.index = "action-*";
        request.query = new Query.Builder().ids(i -> i.values(id)).build();
        List<ActionDocument> documents = actionType.search(request).hits;
        if (documents.isEmpty()) throw new NotFoundException("action not found, id=" + id);
        return documents.get(0);
    }

    private List<ActionDocument> findActionByIds(List<String> ids) {
        var request = new SearchRequest();
        request.index = "action-*";
        request.query = new Query.Builder().ids(i -> i.values(ids)).build();
        return actionType.search(request).hits;
    }

    private List<ActionDocument> findActionByCorrelationIds(List<String> correlationIds) {
        var request = new SearchRequest();
        request.index = "action-*";
        request.query = new Query.Builder().terms(builder -> builder.field("correlation_id").terms(t -> t.value(correlationIds.stream().map(FieldValue::of).toList()))).build();
        request.limit = 10000;
        return actionType.search(request).hits;
    }
}
