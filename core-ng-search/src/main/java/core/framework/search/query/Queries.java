package core.framework.search.query;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.IdsQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery;
import co.elastic.clients.json.JsonData;

import java.util.List;

/**
 * @author neo
 */
public class Queries {
    public static IdsQuery ids(List<String> ids) {
        return new IdsQuery.Builder().values(ids).build();
    }

    public static MatchQuery match(String field, String value) {
        return new MatchQuery.Builder().field(field).query(FieldValue.of(value)).build();
    }

    public static <T> RangeQuery range(String field, T from, T to) {
        var builder = new RangeQuery.Builder().field(field);
        if (from != null) builder.gte(JsonData.of(from));
        if (to != null) builder.lte(JsonData.of(to));
        return builder.build();
    }

    public static TermsQuery terms(String field, List<String> values) {
        return new TermsQuery.Builder().field(field).terms(t -> t.value(values.stream().map(FieldValue::of).toList())).build();
    }

    public static TermQuery term(String field, String value) {
        return new TermQuery.Builder().field(field).value(FieldValue.of(value)).build();
    }

    public static TermQuery term(String field, Boolean value) {
        return new TermQuery.Builder().field(field).value(FieldValue.of(value)).build();
    }
}
