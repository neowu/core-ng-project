package core.framework.search.impl;

import core.framework.api.json.Property;
import core.framework.api.validate.NotNull;
import core.framework.search.Index;
import core.framework.search.SearchRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * @author neo
 */
class ElasticSearchTypeImplTest {
    private ElasticSearchTypeImpl<TestDocument> searchType;

    @BeforeEach
    void createElasticSearchTypeImpl() {
        var search = new ElasticSearchImpl();
        search.maxResultWindow = 100;
        searchType = new ElasticSearchTypeImpl<>(search, TestDocument.class);
    }

    @Test
    void search() {
        var request = new SearchRequest();
        request.skip = 99;
        request.limit = 10;
        assertThatThrownBy(() -> searchType.search(request))
                .isInstanceOf(Error.class)
                .hasMessageContaining("result window is too large");
    }

    @Index(name = "document")
    public static class TestDocument {
        @NotNull
        @Property(name = "id")
        public String id;
    }
}
