package core.framework.search.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ElasticSearchImplTest {
    private ElasticSearchImpl elasticSearch;

    @BeforeEach
    void createElasticSearch() {
        elasticSearch = new ElasticSearchImpl();
    }

    @Test
    void auth() {
        elasticSearch.auth("key", "secret");
        assertThat(elasticSearch.authHeader.getName())
            .isEqualTo("Authorization");
        assertThat(elasticSearch.authHeader.getValue())
            .isEqualTo("ApiKey a2V5OnNlY3JldA==");
    }
}
