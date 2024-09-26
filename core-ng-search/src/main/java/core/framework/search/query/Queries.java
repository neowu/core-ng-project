package core.framework.search.query;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.DateRangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.IdsQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.NumberRangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author neo
 */
public class Queries {
    public static Query ids(List<String> ids) {
        return new Query(new IdsQuery.Builder().values(ids).build());
    }

    public static Query match(String field, String value) {
        return new Query(new MatchQuery.Builder().field(field).query(FieldValue.of(value)).build());
    }

    public static Query range(String field, ZonedDateTime from, ZonedDateTime to) {
        var range = new DateRangeQuery.Builder().field(field);
        if (from != null) range.gte(from.format(DateTimeFormatter.ISO_INSTANT));
        if (to != null) range.lte(to.format(DateTimeFormatter.ISO_INSTANT));
        return new Query(new RangeQuery(range.build()));
    }

    public static Query range(String field, LocalDate from, LocalDate to) {
        var range = new DateRangeQuery.Builder().field(field);
        if (from != null) range.gte(from.format(DateTimeFormatter.ISO_LOCAL_DATE));
        if (to != null) range.lte(to.format(DateTimeFormatter.ISO_LOCAL_DATE));
        return new Query(new RangeQuery(range.build()));
    }

    public static Query range(String field, Number from, Number to) {
        var range = new NumberRangeQuery.Builder().field(field);
        if (from != null) range.gte(from.doubleValue());
        if (to != null) range.lte(to.doubleValue());
        return new Query(new RangeQuery(range.build()));
    }

    public static Query terms(String field, List<String> values) {
        return new Query(new TermsQuery.Builder().field(field).terms(t -> t.value(values.stream().map(FieldValue::of).toList())).build());
    }

    public static Query term(String field, String value) {
        return new Query(new TermQuery.Builder().field(field).value(FieldValue.of(value)).build());
    }

    public static Query term(String field, boolean value) {
        return new Query(new TermQuery.Builder().field(field).value(FieldValue.of(value)).build());
    }
}
