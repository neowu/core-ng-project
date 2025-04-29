package core.framework.search.impl;

import co.elastic.clients.elasticsearch._types.ScriptSortType;
import co.elastic.clients.elasticsearch._types.SearchType;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.json.JsonData;
import core.framework.inject.Inject;
import core.framework.json.JSON;
import core.framework.search.BulkDeleteRequest;
import core.framework.search.ClusterStateResponse;
import core.framework.search.DeleteByQueryRequest;
import core.framework.search.ElasticSearch;
import core.framework.search.ElasticSearchType;
import core.framework.search.ForEach;
import core.framework.search.IntegrationTest;
import core.framework.search.SearchRequest;
import core.framework.search.SearchResponse;
import core.framework.search.query.Sorts;
import core.framework.util.ClasspathResources;
import core.framework.util.Lists;
import core.framework.util.Maps;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static core.framework.search.query.Queries.match;
import static core.framework.search.query.Queries.range;
import static core.framework.search.query.Queries.term;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ElasticSearchIntegrationTest extends IntegrationTest {
    @Inject
    ElasticSearch elasticSearch;
    @Inject
    ElasticSearchType<TestDocument> documentType;

    @AfterEach
    void cleanup() {
        var request = new DeleteByQueryRequest();
        request.query = QueryBuilders.matchAll(m -> m);
        request.refresh = Boolean.TRUE;
        documentType.deleteByQuery(request);
    }

    @Test
    void putIndex() {
        assertThat(elasticSearch.state().metadata.indices).containsKey("document");

        elasticSearch.putIndex("document", ClasspathResources.text("search-test/document-index.json"));
        elasticSearch.putIndex("document", ClasspathResources.text("search-test/document-index.json"));
    }

    @Test
    void index() {
        TestDocument document = document("2", "value2", 2, 0,
            ZonedDateTime.now(),
            LocalTime.of(12, 1, 2, 200000000));
        documentType.index(document.id, document);

        Optional<TestDocument> returnedDocument = documentType.get(document.id);
        assertThat(returnedDocument).get()
            .usingRecursiveComparison()
            .withComparatorForType(ChronoZonedDateTime.timeLineOrder(), ZonedDateTime.class)
            .isEqualTo(document);
    }

    @Test
    void forEach() {
        documentType.bulkIndex(range(0, 30).mapToObj(i -> document(String.valueOf(i), String.valueOf(i), i, 0, null, null))
            .collect(toMap(document -> document.id, identity())));
        elasticSearch.refreshIndex("document");

        List<TestDocument> results = Lists.newArrayList();

        ForEach<TestDocument> forEach = new ForEach<>();
        forEach.query = QueryBuilders.matchAll(m -> m);
        forEach.batchSize = 7;
        forEach.consumer = results::add;

        documentType.forEach(forEach);

        assertThat(results).hasSize(30);
    }

    @Test
    void complete() {
        documentType.bulkIndex(Map.of("1", document("1", "HashSet", 1, 0, null, null),
            "2", document("2", "HashMap", 2, 0, null, null),
            "3", document("3", "TreeSet", 3, 0, null, null),
            "4", document("4", "TreeMap", 4, 0, null, null)));
        elasticSearch.refreshIndex("document");

        List<String> options = documentType.complete("hash", "completion1", "completion2");
        assertThat(options).contains("HashSet-Complete1", "HashSet-Complete2", "HashMap-Complete1", "HashMap-Complete2");
    }

    @Test
    void search() {
        TestDocument document = document("1", "1st Test's Product", 1, 0, null, LocalTime.NOON);
        documentType.index(document.id, document);
        elasticSearch.refreshIndex("document");

        // test synonyms
        var request = new SearchRequest();
        request.type = SearchType.QueryThenFetch;
        request.query = QueryBuilders.bool(b -> b.must(match("string_field", "first"))
            .filter(term("enum_field", JSON.toEnumValue(TestDocument.TestEnum.VALUE1))));

        request.sorts.add(SortOptions.of(builder -> builder.script(s ->
            s.script(script -> script.source(source -> source.scriptString("doc['int_field'].value * 3"))).type(ScriptSortType.Number))));

        SearchResponse<TestDocument> response = documentType.search(request);

        assertThat(response.totalHits).isEqualTo(1);
        assertThat(response.hits).hasSize(1)
            .first().usingRecursiveComparison()
            .withComparatorForType(ChronoZonedDateTime.timeLineOrder(), ZonedDateTime.class)
            .isEqualTo(document);

        // test stemmer
        request = new SearchRequest();
        request.query = match("string_field", "test");
        response = documentType.search(request);

        assertThat(response.totalHits).isEqualTo(1);
        assertThat(response.hits).hasSize(1)
            .first().usingRecursiveComparison()
            .withComparatorForType(ChronoZonedDateTime.timeLineOrder(), ZonedDateTime.class)
            .isEqualTo(document);
    }

    @Test
    void searchDateRange() {
        ZonedDateTime from = ZonedDateTime.now().withNano(999999999);
        ZonedDateTime to = from.plusDays(5).truncatedTo(ChronoUnit.SECONDS);
        documentType.index("1", document("1", "value1", 1, 1.1, from, LocalTime.of(12, 0)));
        documentType.index("2", document("2", "value2", 1, 2.0, from.plusDays(1), LocalTime.of(13, 0)));
        documentType.index("3", document("3", "value3", 1, 3.0, to, LocalTime.of(14, 0)));
        documentType.index("4", document("4", "value4", 1, 4.0, to.plusDays(1), LocalTime.of(15, 0)));
        elasticSearch.refreshIndex("document");

        var request = new SearchRequest();
        request.query = range("zoned_date_time_field", from, to);
        request.sorts.add(Sorts.fieldSort("id", SortOrder.Asc));
        SearchResponse<TestDocument> response = documentType.search(request);
        assertThat(response.totalHits).isEqualTo(3);
        assertThat(response.hits.stream().map(document1 -> document1.stringField).collect(Collectors.toList()))
            .containsOnly("value1", "value2", "value3");

        request.query = QueryBuilders.range(r -> r.untyped(u -> u.field("local_time_field").gt(JsonData.of(LocalTime.of(13, 0)))));
        response = documentType.search(request);
        assertThat(response.totalHits).isEqualTo(2);
        assertThat(response.hits.stream().map(document -> document.stringField).collect(Collectors.toList()))
            .containsOnly("value3", "value4");

        request.query = range("zoned_date_time_field", from.toLocalDate(), to.toLocalDate());
        request.sorts.add(Sorts.fieldSort("id", SortOrder.Asc));
        response = documentType.search(request);
        assertThat(response.totalHits).isEqualTo(3);
        assertThat(response.hits.stream().map(document1 -> document1.stringField).collect(Collectors.toList()))
            .containsOnly("value1", "value2", "value3");
    }

    @Test
    void delete() {
        documentType.index("1", document("1", "value", 1, 0, null, null));

        boolean deleted = documentType.delete("1");
        assertThat(deleted).isTrue();
    }

    @Test
    void deleteByQuery() {
        documentType.bulkIndex(range(0, 30).mapToObj(i -> document(String.valueOf(i), String.valueOf(i), i, 0, null, null))
            .collect(toMap(document -> document.id, identity())));
        elasticSearch.refreshIndex("document");

        var request = new DeleteByQueryRequest();
        request.query = range("int_field", 1, 15);
        request.refresh = Boolean.TRUE;
        long deleted = documentType.deleteByQuery(request);

        assertThat(deleted).isEqualTo(15);
        assertThat(documentType.get("1")).isNotPresent();
        assertThat(documentType.get("15")).isNotPresent();
    }

    @Test
    void bulkDelete() {
        documentType.index("1", document("1", "value1", 1, 0, null, null));
        documentType.index("2", document("2", "value2", 2, 0, null, null));

        var request = new BulkDeleteRequest();
        request.ids = List.of("1", "2");
        request.refresh = Boolean.TRUE;
        documentType.bulkDelete(request);
        assertThat(documentType.get("1")).isNotPresent();
        assertThat(documentType.get("2")).isNotPresent();
    }

    @Test
    void analyze() {
        List<String> tokens = documentType.analyze("standard", "word1 word2");
        assertThat(tokens).contains("word1", "word2");
    }

    @Test
    void state() {
        ClusterStateResponse state = elasticSearch.state();

        assertThat(state.metadata.indices.entrySet()).anyMatch(entry -> "document".equals(entry.getKey()) && entry.getValue().state == ClusterStateResponse.IndexState.OPEN);
    }

    @Test
    void update() {
        documentType.index("4", document("4", "value4", 4, 0, null, null));

        boolean updated = documentType.update("4", "ctx._source.int_field = ctx._source.int_field + params.value", Map.of("value", 1));
        assertThat(updated).isTrue();
        assertThat(documentType.get("4").orElseThrow().intField).isEqualTo(5);

        updated = documentType.update("4", "if (ctx._source.int_field != 5) { ctx._source.int_field = 5 } else { ctx.op = 'noop' }", Map.of());
        assertThat(documentType.get("4").orElseThrow().intField).isEqualTo(5);
        assertThat(updated).isFalse();
    }

    @Test
    void partialUpdate() {
        documentType.index("5", document("5", "value4", 4, 0, null, null));

        var document = new TestDocument();
        document.stringField = "value5";
        document.localTimeField = LocalTime.now();
        boolean updated = documentType.partialUpdate("5", document);
        assertThat(updated).isTrue();

        TestDocument result = documentType.get("5").orElseThrow();
        assertThat(result.stringField).isEqualTo("value5");
        assertThat(result.intField).isEqualTo(4);
        assertThat(result.localTimeField).isEqualTo(document.localTimeField);
        assertThat(result.zonedDateTimeField).isNull();

        updated = documentType.partialUpdate("5", document);
        assertThat(updated).isFalse();
    }

    @Test
    void trackTotalHits() {
        Map<String, TestDocument> documents = Maps.newHashMap();
        for (int i = 0; i < 50; i++) {
            String id = String.valueOf(i);
            documents.put(id, document(id, "value1", 0, 0, null, null));
        }
        documentType.bulkIndex(documents);
        elasticSearch.refreshIndex("document");

        var request = new SearchRequest();
        request.query = new Query.Builder().matchAll(m -> m).build();
        request.limit = 5;
        request.trackTotalHitsUpTo = 10;
        SearchResponse<TestDocument> response = documentType.search(request);

        assertThat(response.hits).hasSize(5);
        assertThat(response.totalHits).isEqualTo(10);

        request.trackTotalHitsUpTo = 20;
        response = documentType.search(request);
        assertThat(response.totalHits).isEqualTo(20);

        request.trackTotalHits();
        response = documentType.search(request);
        assertThat(response.totalHits).isEqualTo(50);
    }

    private TestDocument document(String id, String stringField, int intField, double doubleField, ZonedDateTime dateTimeField, LocalTime timeField) {
        var document = new TestDocument();
        document.id = id;
        document.stringField = stringField;
        document.intField = intField;
        document.doubleField = doubleField;
        document.zonedDateTimeField = dateTimeField;
        document.localTimeField = timeField;
        document.enumField = TestDocument.TestEnum.VALUE1;
        document.completion1 = stringField + "-Complete1";
        document.completion2 = stringField + "-Complete2";
        return document;
    }
}
