package core.framework.search.impl;

import core.framework.inject.Inject;
import core.framework.search.ElasticSearch;
import core.framework.search.ElasticSearchIndex;
import core.framework.search.ElasticSearchType;
import core.framework.search.ForEach;
import core.framework.search.IntegrationTest;
import core.framework.search.SearchRequest;
import core.framework.search.SearchResponse;
import core.framework.util.Lists;
import core.framework.util.Maps;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.sort.ScriptSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        documentType.deleteByQuery(QueryBuilders.matchAllQuery());
        elasticSearch.flush("document");
    }

    @Test
    void index() {
        TestDocument document = document("2", "value2", 2);

        Optional<TestDocument> returnedDocument = documentType.get(document.id);
        assertThat(returnedDocument).get().isEqualToIgnoringGivenFields(document, "zonedDateTimeField");
        assertThat(returnedDocument.orElseThrow().zonedDateTimeField).isEqualTo(document.zonedDateTimeField);
    }

    @Test
    void forEach() {
        Map<String, TestDocument> documents = Maps.newHashMap();
        for (int i = 0; i < 30; i++) {
            TestDocument document = document(String.valueOf(i), String.valueOf(i), i);
            documents.put(document.id, document);
        }
        documentType.bulkIndex(documents);
        elasticSearch.flush("document");

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
        Map<String, TestDocument> documents = Maps.newHashMap();
        documents.put("1", document("1", "HashSet", 1));
        documents.put("2", document("2", "HashMap", 2));
        documents.put("3", document("3", "TreeSet", 3));
        documents.put("4", document("4", "TreeMap", 4));
        documentType.bulkIndex(documents);
        elasticSearch.flush("document");

        List<String> options = documentType.complete("hash", "completion1", "completion2");
        assertThat(options).contains("HashSet-Complete1", "HashSet-Complete2", "HashMap-Complete1", "HashMap-Complete2");
    }

    @Test
    void search() {
        TestDocument document = document("1", "1st Test's Product", 1);
        elasticSearch.flush("document");

        // test synonyms
        SearchRequest request = new SearchRequest();
        request.query = QueryBuilders.matchQuery("string_field", "first");
        request.sorts.add(SortBuilders.scriptSort(new Script("doc['num_field'].value * 3"), ScriptSortBuilder.ScriptSortType.NUMBER));
        SearchResponse<TestDocument> response = documentType.search(request);

        assertThat(response.totalHits).isEqualTo(1);
        assertThat(response.hits.get(0)).isEqualToIgnoringGivenFields(document, "zonedDateTimeField");

        // test stemmer
        request = new SearchRequest();
        request.query = QueryBuilders.matchQuery("string_field", "test");
        response = documentType.search(request);

        assertThat(response.totalHits).isEqualTo(1);
        assertThat(response.hits.get(0)).isEqualToIgnoringGivenFields(document, "zonedDateTimeField");
    }

    @Test
    void delete() {
        TestDocument document = document("1", "value", 1);

        boolean result = documentType.delete(document.id);
        assertThat(result).isTrue();
    }

    @Test
    void bulkDelete() {
        document("1", "value1", 1);
        document("2", "value2", 2);

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
    void indices() {
        List<ElasticSearchIndex> indices = elasticSearch.indices();

        assertThat(indices).hasSize(1);

        ElasticSearchIndex index = indices.get(0);
        assertThat(index.index).isEqualTo("document");
        assertThat(index.state).isEqualTo(IndexMetaData.State.OPEN);
    }

    private TestDocument document(String id, String stringField, int numField) {
        TestDocument document = new TestDocument();
        document.id = id;
        document.stringField = stringField;
        document.numField = numField;
        document.zonedDateTimeField = ZonedDateTime.now(ZoneId.of("America/New_York"));
        document.completion1 = stringField + "-Complete1";
        document.completion2 = stringField + "-Complete2";
        documentType.index(document.id, document);
        return document;
    }
}
