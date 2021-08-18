package core.framework.search.impl;

import org.apache.http.HttpHost;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ElasticSearchHostTest {
    @Test
    void parse() {
        assertThat(ElasticSearchHost.parse("es-0"))
            .containsExactly(new HttpHost("es-0", 9200));

        assertThat(ElasticSearchHost.parse("es-0, es-1"))
            .containsExactly(new HttpHost("es-0", 9200), new HttpHost("es-1", 9200));

        assertThat(ElasticSearchHost.parse("https://test1.azure.elastic-cloud.com:9243, http://test2.azure.elastic-cloud.com:9200"))
            .containsExactly(new HttpHost("test1.azure.elastic-cloud.com", 9243, "https"), new HttpHost("test2.azure.elastic-cloud.com", 9200, "http"));
    }
}
