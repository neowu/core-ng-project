package core.framework.search.impl;

import core.framework.inject.Inject;
import core.framework.json.JSON;
import core.framework.search.ClusterStateResponse;
import core.framework.search.ElasticSearch;
import core.framework.search.ElasticSearchType;
import core.framework.search.ForEach;
import core.framework.search.IntegrationTest;
import core.framework.search.SearchRequest;
import core.framework.search.SearchResponse;
import core.framework.util.ClasspathResources;
import core.framework.util.Lists;
import core.framework.util.Maps;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.Sum;
import org.elasticsearch.search.sort.ScriptSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;
import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

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
        documentType.bulkDelete(range(0, 100).mapToObj(String::valueOf).collect(Collectors.toList()));
        elasticSearch.refreshIndex("document");
    }

    @Test
    void createIndex() {
        assertThat(elasticSearch.state().metadata.indices).containsKey("document");

        elasticSearch.createIndex("document", ClasspathResources.text("search-test/document-index.json"));
    }

    @Test
    void index() {
        TestDocument document = document("2", "value2", 2, 0,
            ZonedDateTime.now(),
            LocalTime.of(12, 1, 2, 200000000));
        documentType.index(document.id, document);

        Optional<TestDocument> returnedDocument = documentType.get(document.id);
        assertThat(returnedDocument).get().isEqualToIgnoringGivenFields(document, "zonedDateTimeField");
        assertThat(returnedDocument.orElseThrow().zonedDateTimeField).isEqualTo(document.zonedDateTimeField);
    }

    @Test
    void forEach() {
        documentType.bulkIndex(range(0, 30).mapToObj(i -> document(String.valueOf(i), String.valueOf(i), i, 0, null, null))
                                           .collect(toMap(document -> document.id, identity())));
        elasticSearch.refreshIndex("document");

        List<TestDocument> results = Lists.newArrayList();

        ForEach<TestDocument> forEach = new ForEach<>();
        forEach.query = QueryBuilders.matchAllQuery();
        forEach.limit = 7;
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
        SearchRequest request = new SearchRequest();
        request.query = boolQuery()
            .must(matchQuery("string_field", "first"))
            .filter(termQuery("enum_field", JSON.toEnumValue(TestDocument.TestEnum.VALUE1)));
        request.sorts.add(SortBuilders.scriptSort(new Script("doc['int_field'].value * 3"), ScriptSortBuilder.ScriptSortType.NUMBER));
        SearchResponse<TestDocument> response = documentType.search(request);

        assertThat(response.totalHits).isEqualTo(1);
        assertThat(response.hits.get(0)).isEqualToIgnoringGivenFields(document, "zonedDateTimeField");

        // test stemmer
        request = new SearchRequest();
        request.query = matchQuery("string_field", "test");
        response = documentType.search(request);

        assertThat(response.totalHits).isEqualTo(1);
        assertThat(response.hits.get(0)).isEqualToIgnoringGivenFields(document, "zonedDateTimeField");
    }

    @Test
    void searchDateRange() {
        ZonedDateTime from = ZonedDateTime.now();
        ZonedDateTime to = from.plusDays(5);
        documentType.index("1", document("1", "value1", 1, 0, from, LocalTime.of(12, 0)));
        documentType.index("2", document("2", "value2", 1, 0, from.plusDays(1), LocalTime.of(13, 0)));
        documentType.index("3", document("3", "value3", 1, 0, to, LocalTime.of(14, 0)));
        documentType.index("4", document("4", "value4", 1, 0, to.plusDays(1), LocalTime.of(15, 0)));
        elasticSearch.refreshIndex("document");

        var request = new SearchRequest();
        request.query = rangeQuery("zoned_date_time_field").from(from).to(to);
        SearchResponse<TestDocument> response = documentType.search(request);
        assertThat(response.totalHits).isEqualTo(3);
        assertThat(response.hits.stream().map(document1 -> document1.stringField).collect(Collectors.toList()))
            .containsOnly("value1", "value2", "value3");

        request.query = rangeQuery("local_time_field").gt(LocalTime.of(13, 0));
        response = documentType.search(request);
        assertThat(response.totalHits).isEqualTo(2);
        assertThat(response.hits.stream().map(document -> document.stringField).collect(Collectors.toList()))
            .containsOnly("value3", "value4");
    }

    @Test
    void delete() {
        documentType.index("1", document("1", "value", 1, 0, null, null));
        elasticSearch.refreshIndex("document");

        boolean result = documentType.delete("1");
        assertThat(result).isTrue();
    }

    @Test
    void bulkDelete() {
        documentType.index("1", document("1", "value1", 1, 0, null, null));
        documentType.index("2", document("2", "value2", 2, 0, null, null));
        elasticSearch.refreshIndex("document");

        documentType.bulkDelete(List.of("1", "2"));
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

        documentType.update("4", "ctx._source.int_field = ctx._source.int_field + params.value", Map.of("value", 1));

        assertThat(documentType.get("4").orElseThrow().intField).isEqualTo(5);
    }

    @Test
    void aggregate() {
        documentType.index("1", document("1", "value1", 0, 19.13, null, null));
        documentType.index("2", document("2", "value1", 0, 0.01, null, null));
        documentType.index("3", document("3", "value3", 0, 1.5, null, null));
        elasticSearch.refreshIndex("document");

        var request = new SearchRequest();
        request.skip = 0;
        request.limit = 1;
        request.query = matchQuery("string_field", "value1");
        request.aggregations.add(AggregationBuilders.sum("totalValue").field("double_field"));
        SearchResponse<TestDocument> response = documentType.search(request);

        assertThat(response.totalHits).isEqualTo(2);
        assertThat(response.hits).hasSize(1);
        assertThat(response.aggregations).containsKeys("totalValue");

        var sum = new BigDecimal(((Sum) response.aggregations.get("totalValue")).getValue()).setScale(4, RoundingMode.HALF_UP);
        assertThat(sum).isEqualTo("19.1400");
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
        request.query = matchAllQuery();
        request.limit = 5;
        request.trackTotalHitsUpTo = 10;
        SearchResponse<TestDocument> response = documentType.search(request);

        assertThat(response.hits).hasSize(5);
        assertThat(response.totalHits).isEqualTo(10);

        request.trackTotalHitsUpTo = 20;
        response = documentType.search(request);
        assertThat(response.totalHits).isEqualTo(20);
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
