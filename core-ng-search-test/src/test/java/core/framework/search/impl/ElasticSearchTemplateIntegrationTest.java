package core.framework.search.impl;

import core.framework.inject.Inject;
import core.framework.search.ClusterStateResponse;
import core.framework.search.ElasticSearch;
import core.framework.search.ElasticSearchType;
import core.framework.search.IndexRequest;
import core.framework.search.IntegrationTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ElasticSearchTemplateIntegrationTest extends IntegrationTest {
    @Inject
    ElasticSearch elasticSearch;
    @Inject
    ElasticSearchType<TestDocument> documentType;

    @Test
    void closeIndex() {
        index("document-1", "1", "text-1", 2);
        elasticSearch.closeIndex("document-1");
        ClusterStateResponse.Index index = elasticSearch.state().metadata.indices.get("document-1");
        assertThat(index.state).isEqualTo(ClusterStateResponse.IndexState.CLOSE);
    }

    @Test
    void deleteIndex() {
        index("document-2", "2", "text-2", 3);
        elasticSearch.deleteIndex("document-2");
        assertThat(elasticSearch.state().metadata.indices).doesNotContainKeys("document-2");
    }

    private void index(String index, String id, String stringField, int numField) {
        TestDocument document = new TestDocument();
        document.id = id;
        document.stringField = stringField;
        document.numField = numField;
        document.completion1 = stringField + "-Complete1";
        document.completion2 = stringField + "-Complete2";
        IndexRequest<TestDocument> request = new IndexRequest<>();
        request.id = id;
        request.source = document;
        request.index = index;
        documentType.index(request);
    }
}
