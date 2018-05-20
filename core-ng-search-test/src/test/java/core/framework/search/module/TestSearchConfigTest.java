package core.framework.search.module;

import core.framework.search.impl.ElasticSearchImpl;
import core.framework.search.impl.MockElasticSearch;
import core.framework.test.inject.TestBeanFactory;
import core.framework.test.module.TestModuleContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class TestSearchConfigTest {
    private TestSearchConfig config;

    @BeforeEach
    void createTestSearchConfig() {
        config = new TestSearchConfig();
    }

    @Test
    void createElasticSearch() {
        ElasticSearchImpl search = config.createElasticSearch(new TestModuleContext(new TestBeanFactory()));
        assertThat(search).isInstanceOf(MockElasticSearch.class);

        assertThat(System.getProperty("log4j.configurationFactory")).isNotNull();
    }
}
