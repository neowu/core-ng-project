package core.framework.search.module;

import core.framework.search.impl.MockElasticSearch;
import core.framework.test.inject.TestBeanFactory;
import core.framework.test.module.TestModuleContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class TestSearchConfigTest {
    @Test
    void initialize() {
        TestSearchConfig config = new TestSearchConfig(new TestModuleContext(new TestBeanFactory()));
        assertThat(config.search).isInstanceOf(MockElasticSearch.class);

        assertThat(System.getProperty("log4j.configurationFactory")).isNotNull();
    }
}
