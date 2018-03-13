package core.framework.test.search;

import core.framework.inject.Inject;
import core.framework.search.ElasticSearch;
import core.framework.search.ElasticSearchIndex;
import core.framework.search.ElasticSearchType;
import core.framework.search.ForEach;
import core.framework.search.SearchRequest;
import core.framework.search.SearchResponse;
import core.framework.test.IntegrationTest;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        TestDocument document = createDocument("2", "value2", 2);

        Optional<TestDocument> returnedDocument = documentType.get(document.id);
        assertTrue(returnedDocument.isPresent());
        assertEquals(document.stringField, returnedDocument.get().stringField);
        assertEquals(document.zonedDateTimeField.toInstant(), returnedDocument.get().zonedDateTimeField.toInstant());
    }

    @Test
    void forEach() {
        Map<String, TestDocument> documents = Maps.newHashMap();
        for (int i = 0; i < 30; i++) {
            TestDocument document = new TestDocument();
            document.id = String.valueOf(i);
            document.stringField = String.valueOf(i);
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

        assertEquals(30, results.size());
    }

    @Test
    void search() {
        TestDocument document = createDocument("1", "value1", 1);
        elasticSearch.flush("document");

        SearchRequest request = new SearchRequest();
        request.query = QueryBuilders.matchQuery("string_field", document.stringField);
        request.sorts.add(SortBuilders.scriptSort(new Script("doc['num_field'].value * 3"), ScriptSortBuilder.ScriptSortType.NUMBER));
        SearchResponse<TestDocument> response = documentType.search(request);

        assertEquals(1, response.totalHits);
        TestDocument returnedDocument = response.hits.get(0);
        assertEquals(document.stringField, returnedDocument.stringField);
    }

    @Test
    void delete() {
        TestDocument document = createDocument("1", "value", 1);

        boolean result = documentType.delete(document.id);
        assertTrue(result);
    }

    @Test
    void bulkDelete() {
        createDocument("1", "value1", 1);
        createDocument("2", "value2", 2);

        documentType.bulkDelete(Lists.newArrayList("1", "2"));
        assertFalse(documentType.get("1").isPresent());
        assertFalse(documentType.get("2").isPresent());
    }

    @Test
    void analyze() {
        List<String> tokens = documentType.analyze("standard", "word1 word2");
        assertEquals(Lists.newArrayList("word1", "word2"), tokens);
    }

    @Test
    void indices() {
        List<ElasticSearchIndex> indices = elasticSearch.indices();

        assertEquals(1, indices.size());
        ElasticSearchIndex index = indices.get(0);
        assertEquals("document", index.index);
        assertEquals(IndexMetaData.State.OPEN, index.state);
    }

    private TestDocument createDocument(String id, String stringField, int numField) {
        TestDocument document = new TestDocument();
        document.id = id;
        document.stringField = stringField;
        document.numField = numField;
        document.zonedDateTimeField = ZonedDateTime.now(ZoneId.of("America/New_York"));
        documentType.index(document.id, document);
        return document;
    }
}
