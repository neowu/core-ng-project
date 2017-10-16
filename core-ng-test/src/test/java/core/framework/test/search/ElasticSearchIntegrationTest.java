package core.framework.test.search;

import core.framework.search.BulkIndexRequest;
import core.framework.search.ElasticSearch;
import core.framework.search.ElasticSearchType;
import core.framework.search.ForEach;
import core.framework.search.SearchRequest;
import core.framework.search.SearchResponse;
import core.framework.test.IntegrationTest;
import core.framework.util.Lists;
import core.framework.util.Maps;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.sort.ScriptSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        TestDocument document = new TestDocument();
        document.id = "1";
        document.stringField = "value";
        document.zonedDateTimeField = ZonedDateTime.now(ZoneId.of("America/New_York"));
        documentType.index(document.id, document);

        Optional<TestDocument> returnedDocument = documentType.get("1");
        assertTrue(returnedDocument.isPresent());
        assertEquals(document.stringField, returnedDocument.get().stringField);
        assertEquals(document.zonedDateTimeField.toInstant(), returnedDocument.get().zonedDateTimeField.toInstant());
    }

    @Test
    void forEach() {
        BulkIndexRequest<TestDocument> request = new BulkIndexRequest<>();
        request.sources = Maps.newHashMap();
        for (int i = 0; i < 30; i++) {
            TestDocument document = new TestDocument();
            document.id = String.valueOf(i);
            document.stringField = String.valueOf(i);
            request.sources.put(document.id, document);
        }
        documentType.bulkIndex(request);
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
        TestDocument document = new TestDocument();
        document.id = "1";
        document.numField = 1;
        document.stringField = "value";
        documentType.index(document.id, document);
        elasticSearch.flush("document");

        SearchRequest request = new SearchRequest();
        request.query = QueryBuilders.matchQuery("string_field", document.stringField);
        request.sorts.add(SortBuilders.scriptSort(new Script("doc['num_field'].value * 3"), ScriptSortBuilder.ScriptSortType.NUMBER));
        SearchResponse<TestDocument> response = documentType.search(request);

        assertEquals(1, response.totalHits);
        TestDocument returnedDocument = response.hits.get(0);
        assertEquals(document.stringField, returnedDocument.stringField);
    }
}
