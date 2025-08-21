package core.framework.internal.db;

import core.framework.db.Dialect;
import core.framework.util.ClasspathResources;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class InsertQueryBuilderTest {
    @Test
    void assignedId() {
        InsertQueryBuilder<AssignedIdEntity> builder = new InsertQueryBuilder<>(AssignedIdEntity.class, Dialect.MYSQL);
        InsertQuery<AssignedIdEntity> query = builder.build();

        assertThat(builder.builder.sourceCode()).isEqualTo(ClasspathResources.text("db-test/insert-query-param-builder-assigned-id.java"));

        assertThat(query.insertSQL).isEqualTo("INSERT INTO assigned_id_entity (id, string_field, int_field, big_decimal_field, date_field, zoned_date_time_field) VALUES (?, ?, ?, ?, ?, ?)");
        assertThat(query.insertIgnoreSQL).isEqualTo("INSERT IGNORE INTO assigned_id_entity (id, string_field, int_field, big_decimal_field, date_field, zoned_date_time_field) VALUES (?, ?, ?, ?, ?, ?)");
        assertThat(query.upsertSQL).isEqualTo("INSERT INTO assigned_id_entity (id, string_field, int_field, big_decimal_field, date_field, zoned_date_time_field) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE string_field = VALUES(string_field), int_field = VALUES(int_field), big_decimal_field = VALUES(big_decimal_field), date_field = VALUES(date_field), zoned_date_time_field = VALUES(zoned_date_time_field)");
        assertThat(query.generatedColumn).isNull();
    }

    @Test
    void assignedIdWithPostgreSQL() {
        InsertQueryBuilder<AssignedIdEntity> builder = new InsertQueryBuilder<>(AssignedIdEntity.class, Dialect.POSTGRESQL);
        InsertQuery<AssignedIdEntity> query = builder.build();

        assertThat(builder.builder.sourceCode()).isEqualTo(ClasspathResources.text("db-test/insert-query-param-builder-assigned-id.java"));

        assertThat(query.insertSQL).isEqualTo("INSERT INTO assigned_id_entity (id, string_field, int_field, big_decimal_field, date_field, zoned_date_time_field) VALUES (?, ?, ?, ?, ?, ?)");
        assertThat(query.insertIgnoreSQL).isEqualTo("INSERT INTO assigned_id_entity (id, string_field, int_field, big_decimal_field, date_field, zoned_date_time_field) VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING");
        assertThat(query.upsertSQL).isEqualTo("INSERT INTO assigned_id_entity (id, string_field, int_field, big_decimal_field, date_field, zoned_date_time_field) VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT (id) DO UPDATE SET string_field = EXCLUDED.string_field, int_field = EXCLUDED.int_field, big_decimal_field = EXCLUDED.big_decimal_field, date_field = EXCLUDED.date_field, zoned_date_time_field = EXCLUDED.zoned_date_time_field");
        assertThat(query.generatedColumn).isNull();
    }

    @Test
    void autoIncrementId() {
        InsertQueryBuilder<AutoIncrementIdEntity> builder = new InsertQueryBuilder<>(AutoIncrementIdEntity.class, Dialect.MYSQL);
        InsertQuery<AutoIncrementIdEntity> query = builder.build();

        assertThat(builder.builder.sourceCode()).isEqualTo(ClasspathResources.text("db-test/insert-query-param-builder-auto-increment-id.java"));

        assertThat(query.insertSQL).isEqualTo("INSERT INTO auto_increment_id_entity (string_field, double_field, enum_field, date_time_field, zoned_date_time_field) VALUES (?, ?, ?, ?, ?)");
        assertThat(query.insertIgnoreSQL).isNull();
        assertThat(query.upsertSQL).isNull();
        assertThat(query.generatedColumn).isEqualTo("id");
    }

    @Test
    void compositeId() {
        InsertQueryBuilder<CompositeKeyEntity> builder = new InsertQueryBuilder<>(CompositeKeyEntity.class, Dialect.MYSQL);
        InsertQuery<CompositeKeyEntity> query = builder.build();

        assertThat(query.upsertSQL).isEqualTo("INSERT INTO composite_key_entity (id1, id_2, boolean_field, long_field) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE boolean_field = VALUES(boolean_field), long_field = VALUES(long_field)");
    }

    @Test
    void compositeIdWithPostgreSQL() {
        InsertQueryBuilder<CompositeKeyEntity> builder = new InsertQueryBuilder<>(CompositeKeyEntity.class, Dialect.POSTGRESQL);
        InsertQuery<CompositeKeyEntity> query = builder.build();
        assertThat(query.upsertSQL).isEqualTo("INSERT INTO composite_key_entity (id1, id_2, boolean_field, long_field) VALUES (?, ?, ?, ?) ON CONFLICT (id1, id_2) DO UPDATE SET boolean_field = EXCLUDED.boolean_field, long_field = EXCLUDED.long_field");
    }
}
