package core.framework.search.query;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.DateRangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.NumberRangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author neo
 */
public class Queries {
    public static Query ids(List<String> ids) {
        return QueryBuilders.ids().values(ids).build()._toQuery();
    }

    public static Query match(String field, String value) {
        return QueryBuilders.match().field(field).query(value).build()._toQuery();
    }

    public static Query matchPhase(String field, String query) {
        return QueryBuilders.matchPhrase().field(field).query(query).build()._toQuery();
    }

    public static Query range(String field, @Nullable ZonedDateTime from, @Nullable ZonedDateTime to) {
        var range = new DateRangeQuery.Builder().field(field);
        if (from != null) range.gte(from.format(DateTimeFormatter.ISO_INSTANT));
        if (to != null) range.lte(to.format(DateTimeFormatter.ISO_INSTANT));
        return QueryBuilders.range().date(range.build()).build()._toQuery();
    }

    public static Query range(String field, @Nullable LocalDate from, @Nullable LocalDate to) {
        var range = new DateRangeQuery.Builder().field(field);
        if (from != null) range.gte(from.format(DateTimeFormatter.ISO_LOCAL_DATE));
        if (to != null) range.lte(to.format(DateTimeFormatter.ISO_LOCAL_DATE));
        return QueryBuilders.range().date(range.build()).build()._toQuery();
    }

    public static Query range(String field, @Nullable Number from, @Nullable Number to) {
        var range = new NumberRangeQuery.Builder().field(field);
        if (from != null) range.gte(from.doubleValue());
        if (to != null) range.lte(to.doubleValue());
        return QueryBuilders.range().number(range.build()).build()._toQuery();
    }

    public static Query terms(String field, List<String> values) {
        return QueryBuilders.terms().field(field).terms(t -> t.value(values.stream().map(FieldValue::of).toList())).build()._toQuery();
    }

    public static Query term(String field, String value) {
        return QueryBuilders.term().field(field).value(FieldValue.of(value)).build()._toQuery();
    }

    public static Query term(String field, boolean value) {
        return QueryBuilders.term().field(field).value(FieldValue.of(value)).build()._toQuery();
    }
}
