package core.framework.search.query;

import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class QueriesTest {
    @Test
    void terms() {
        TermsQuery terms = Queries.terms("field", List.of("value1", "value2"));
        assertThat(terms.terms().value().get(0).stringValue()).isEqualTo("value1");
        assertThat(terms.terms().value().get(1).stringValue()).isEqualTo("value2");
    }
}
