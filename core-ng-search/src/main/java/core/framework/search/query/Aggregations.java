package core.framework.search.query;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;

/**
 * @author neo
 */
public class Aggregations {
    public static Aggregation sum(String field) {
        return Aggregation.of(a -> a.sum(s -> s.field(field)));
    }
}
