package core.framework.search.impl;

import org.apache.http.HttpHost;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class ElasticSearchHostTest {
    @Test
    void parse() {
        assertThat(ElasticSearchHost.parse("es-0"))
            .containsExactly(new HttpHost("es-0", 9200));

        assertThat(ElasticSearchHost.parse("es-0:9300"))
            .containsExactly(new HttpHost("es-0", 9300));

        assertThat(ElasticSearchHost.parse("http://es-0"))
            .containsExactly(new HttpHost("es-0", 9200));

        assertThat(ElasticSearchHost.parse("http://es-0, http://es-1"))
            .containsExactly(new HttpHost("es-0", 9200), new HttpHost("es-1", 9200));
    }

    @Test
    void parseWithHTTPS() {
        assertThat(ElasticSearchHost.parse("https://es-cloud.io"))
            .containsExactly(new HttpHost("es-cloud.io", 9200, "https"));

        assertThat(ElasticSearchHost.parse("https://es-cloud.io:9545"))
            .containsExactly(new HttpHost("es-cloud.io", 9545, "https"));
    }

    @Test
    void parseWithInvalidHost() {
        assertThatThrownBy(() -> ElasticSearchHost.parse("es-0:"))
            .isInstanceOf(Error.class)
            .hasMessageContaining("invalid elasticsearch host");

        assertThatThrownBy(() -> ElasticSearchHost.parse("http://"))
            .isInstanceOf(Error.class)
            .hasMessageContaining("invalid elasticsearch host");
    }
}
