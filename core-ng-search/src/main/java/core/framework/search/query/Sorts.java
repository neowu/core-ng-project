package core.framework.search.query;

import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;

/**
 * @author neo
 */
public class Sorts {
    public static SortOptions fieldSort(String field, SortOrder order) {
        return SortOptions.of(s -> s.field(f -> f.field(field).order(order)));
    }
}
