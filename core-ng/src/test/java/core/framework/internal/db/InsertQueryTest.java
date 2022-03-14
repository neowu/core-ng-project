package core.framework.internal.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class InsertQueryTest {
    private InsertQuery<Object> query;

    @BeforeEach
    void createInsertQuery() {
        query = new InsertQuery<>("INSERT INTO test (id, value) VALUES (?, ?)", null, null, param -> new Object[]{1, "value"});
    }

    @Test
    void insertIgnoreSQL() {
        assertThat(query.insertIgnoreSQL()).isEqualTo("INSERT IGNORE INTO test (id, value) VALUES (?, ?)");
    }
}
