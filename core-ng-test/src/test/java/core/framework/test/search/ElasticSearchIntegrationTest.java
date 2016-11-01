package core.framework.test.search;

import core.framework.api.search.BulkIndexRequest;
import core.framework.api.search.ElasticSearch;
import core.framework.api.search.ElasticSearchType;
import core.framework.api.search.ForEach;
import core.framework.api.search.SearchRequest;
import core.framework.api.search.SearchResponse;
import core.framework.api.util.Lists;
import core.framework.api.util.Maps;
import core.framework.test.IntegrationTest;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;

import javax.inject.Inject;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author neo
 */
public class ElasticSearchIntegrationTest extends IntegrationTest {
    @Inject
    ElasticSearch elasticSearch;
    @Inject
    ElasticSearchType<TestDocument> documentType;

    @Test
    public void index() {
        TestDocument document = new TestDocument();
        document.stringField = "value";
        document.zonedDateTimeField = ZonedDateTime.now(ZoneId.of("America/New_York"));
        documentType.index("1", document);

        Optional<TestDocument> returnedDocument = documentType.get("1");
        assertTrue(returnedDocument.isPresent());
        assertEquals(document.stringField, returnedDocument.get().stringField);
        assertEquals(document.zonedDateTimeField.toInstant(), returnedDocument.get().zonedDateTimeField.toInstant());
    }

    @Test
    public void forEach() {
        BulkIndexRequest<TestDocument> request = new BulkIndexRequest<>();
        request.sources = Maps.newHashMap();
        for (int i = 0; i < 30; i++) {
            TestDocument document = new TestDocument();
            document.stringField = String.valueOf(i);
            request.sources.put(String.valueOf(i), document);
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
    public void search() {
        SearchRequest request = new SearchRequest();
        request.query = QueryBuilders.matchAllQuery();
        SearchResponse<TestDocument> response = documentType.search(request);

        assertNotNull(response);
    }
}
