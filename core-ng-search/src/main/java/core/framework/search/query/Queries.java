package core.framework.search.query;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.DateRangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.IdsQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.NumberRangeQuery;
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
    public static IdsQuery ids(List<String> ids) {
        return new IdsQuery.Builder().values(ids).build();
    }

    public static MatchQuery match(String field, String value) {
        return new MatchQuery.Builder().field(field).query(FieldValue.of(value)).build();
    }

    public static RangeQuery range(String field, ZonedDateTime from, ZonedDateTime to) {
        var range = new DateRangeQuery.Builder().field(field);
        if (from != null) range.gte(from.format(DateTimeFormatter.ISO_INSTANT));
        if (to != null) range.lte(to.format(DateTimeFormatter.ISO_INSTANT));
        var builder = new RangeQuery.Builder().date(range.build());
        return builder.build();
    }

    public static RangeQuery range(String field, LocalDate from, LocalDate to) {
        var range = new DateRangeQuery.Builder().field(field);
        if (from != null) range.gte(from.format(DateTimeFormatter.ISO_LOCAL_DATE));
        if (to != null) range.lte(to.format(DateTimeFormatter.ISO_LOCAL_DATE));
        var builder = new RangeQuery.Builder().date(range.build());
        return builder.build();
    }

    public static RangeQuery range(String field, Number from, Number to) {
        var range = new NumberRangeQuery.Builder().field(field);
        if (from != null) range.gte(from.doubleValue());
        if (to != null) range.lte(to.doubleValue());
        var builder = new RangeQuery.Builder().number(range.build());
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
