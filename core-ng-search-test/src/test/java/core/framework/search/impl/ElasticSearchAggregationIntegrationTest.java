package core.framework.search.impl;

import co.elastic.clients.elasticsearch._types.aggregations.DateHistogramBucket;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import core.framework.inject.Inject;
import core.framework.search.DeleteByQueryRequest;
import core.framework.search.ElasticSearch;
import core.framework.search.ElasticSearchType;
import core.framework.search.IntegrationTest;
import core.framework.search.SearchRequest;
import core.framework.search.SearchResponse;
import core.framework.util.ClasspathResources;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.ZonedDateTime;
import java.util.List;

import static core.framework.search.query.Aggregations.sum;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ElasticSearchAggregationIntegrationTest extends IntegrationTest {
    @Inject
    ElasticSearch elasticSearch;
    @Inject
    ElasticSearchType<TestAggregationDocument> documentType;

    @BeforeAll
    void initialize() {
        documentType.index("1", document(ZonedDateTime.parse("2024-08-09T00:00:00Z"), "a1", "b1", 1));
        documentType.index("2", document(ZonedDateTime.parse("2024-08-09T00:00:00Z"), "a2", "b2", 2));
        documentType.index("3", document(ZonedDateTime.parse("2024-08-10T00:00:00Z"), "a1", "b2", 3));
        documentType.index("4", document(ZonedDateTime.parse("2024-08-10T00:00:00Z"), "a2", "b2", 4));
        documentType.index("5", document(ZonedDateTime.parse("2024-08-11T00:00:00Z"), "a1", "b1", 5));
        documentType.index("6", document(ZonedDateTime.parse("2024-08-11T00:00:00Z"), "a1", "b2", 6));
        elasticSearch.refreshIndex("aggregation_document");
    }

    @AfterAll
    void cleanup() {
        var request = new DeleteByQueryRequest();
        request.query = new Query.Builder().matchAll(b -> b).build();
        request.refresh = Boolean.TRUE;
        documentType.deleteByQuery(request);
    }

    @Test
    void aggregate() {
        var request = new SearchRequest();
        request.limit = 1;
        request.aggregations.put("total_value", sum("value"));
        SearchResponse<TestAggregationDocument> response = documentType.search(request);

        assertThat(response.totalHits).isEqualTo(6);
        assertThat(response.hits).hasSize(1);
        assertThat(response.aggregations).containsKeys("total_value");

        int sum = response.aggregations.get("total_value").sum().value().intValue();
        assertThat(sum).isEqualTo(21);
    }

    @Test
    void subAggregate() {
        var request = new SearchRequest();
        request.withJSON(ClasspathResources.text("search-test/sub-aggregation.json"));
        request.limit = 0;

        SearchResponse<TestAggregationDocument> response = documentType.search(request);
        List<DateHistogramBucket> dates = response.aggregations.get("date").dateHistogram().buckets().array();
        assertThat(dates.getFirst().keyAsString())
            .isEqualTo("2024-08-09T00:00:00.000Z");
        assertThat(dates.getFirst().aggregations().get("key_1").sterms().buckets().array().getFirst().key().stringValue())
            .isEqualTo("a2");
        assertThat(dates.getFirst().aggregations().get("key_1").sterms().buckets().array().getFirst().aggregations().get("total_value").sum().value())
            .isEqualTo(2);
        assertThat(dates.getFirst().aggregations().get("key_1").sterms().buckets().array().get(1).key().stringValue())
            .isEqualTo("a1");
        assertThat(dates.getFirst().aggregations().get("key_1").sterms().buckets().array().get(1).aggregations().get("total_value").sum().value())
            .isEqualTo(1);

        assertThat(dates.get(2).keyAsString())
            .isEqualTo("2024-08-11T00:00:00.000Z");
        assertThat(dates.get(2).aggregations().get("key_1").sterms().buckets().array().getFirst().key().stringValue())
            .isEqualTo("a1");
        assertThat(dates.get(2).aggregations().get("key_1").sterms().buckets().array().getFirst().aggregations().get("total_value").sum().value())
            .isEqualTo(11);
    }

    @Test
    void subAggregateWithRuntimeField() {
        var request = new SearchRequest();
        request.withJSON(ClasspathResources.text("search-test/sub-aggregation-with-runtime-field.json"));
        request.limit = 0;

        SearchResponse<TestAggregationDocument> response = documentType.search(request);
        List<StringTermsBucket> buckets = response.aggregations.get("composited_key").sterms().buckets().array();
        assertThat(buckets.getFirst().key().stringValue())
            .isEqualTo("a1|b2");
        assertThat(buckets.getFirst().aggregations().get("total_value").sum().value())
            .isEqualTo(9);
    }

    private TestAggregationDocument document(ZonedDateTime date, String key1, String key2, int value) {
        var document = new TestAggregationDocument();
        document.date = date;
        document.key1 = key1;
        document.key2 = key2;
        document.value = value;
        return document;
    }
}
