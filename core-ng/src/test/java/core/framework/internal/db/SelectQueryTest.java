package core.framework.internal.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class SelectQueryTest {
    private SelectQuery selectQuery;

    @BeforeEach
    void createSelectQuery() {
        selectQuery = new SelectQuery<>(AssignedIdEntity.class);
    }

    @Test
    void fetchParams() {
        Object[] params = selectQuery.fetchParams(List.of("value"), null, 100);

        assertThat(params).containsExactly("value", 0, 100);    // default skip should be 0
    }
}
