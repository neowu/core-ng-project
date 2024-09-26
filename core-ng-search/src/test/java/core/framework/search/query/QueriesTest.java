package core.framework.search.query;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author neo
 */
class QueriesTest {
    @Test
    void terms() {
        Query query = Queries.terms("field", List.of("value1", "value2"));
        assertThat(query.isTerms()).isTrue();
        assertThat(query.terms().terms().value().get(0).stringValue()).isEqualTo("value1");
        assertThat(query.terms().terms().value().get(1).stringValue()).isEqualTo("value2");
    }

    @Test
    void ids() {
        Query query = Queries.ids(List.of("id1", "id2"));
        assertThat(query.isIds()).isTrue();
        assertThat(query.ids().values()).isEqualTo(List.of("id1", "id2"));
    }

    @Test
    void matchPhrase() {
        Query query = Queries.matchPhase("field", "query");
        assertThat(query.isMatchPhrase()).isTrue();
        assertThat(query.matchPhrase().field()).isEqualTo("field");
        assertThat(query.matchPhrase().query()).isEqualTo("query");
    }
}
