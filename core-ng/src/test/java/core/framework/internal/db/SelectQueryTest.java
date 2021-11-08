package core.framework.internal.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class SelectQueryTest {
    private SelectQuery<AssignedIdEntity> selectQuery;

    @BeforeEach
    void createSelectQuery() {
        selectQuery = new SelectQuery<>(AssignedIdEntity.class);
    }

    @Test
    void getSQL() {
        assertThat(selectQuery.getSQL).isEqualTo("SELECT id, string_field, int_field, big_decimal_field, date_field, zoned_date_time_field FROM assigned_id_entity WHERE id = ?");
    }

    @Test
    void fetchSQL() {
        String sql = selectQuery.fetchSQL(new StringBuilder("string_field = ?"), "int_field ASC", 4, 10);
        assertThat(sql).isEqualTo("SELECT id, string_field, int_field, big_decimal_field, date_field, zoned_date_time_field FROM assigned_id_entity WHERE string_field = ? ORDER BY int_field ASC LIMIT ?,?");
    }

    @Test
    void fetchParams() {
        Object[] params = selectQuery.fetchParams(List.of("value"), null, 100);

        assertThat(params).containsExactly("value", 0, 100);    // default skip should be 0
    }
}
