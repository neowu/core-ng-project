package core.framework.impl.db;

import core.framework.util.ClasspathResources;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class InsertQueryBuilderTest {
    @Test
    void assignedId() {
        InsertQueryBuilder<AssignedIdEntity> builder = new InsertQueryBuilder<>(AssignedIdEntity.class);
        InsertQuery<AssignedIdEntity> query = builder.build();

        assertThat(builder.builder.sourceCode()).isEqualTo(ClasspathResources.text("db-test/insert-query-param-builder-assigned-id.java"));

        assertThat(query.sql).isEqualTo("INSERT INTO assigned_id_entity (id, string_field, int_field, big_decimal_field, date_field) VALUES (?, ?, ?, ?, ?)");
        assertThat(query.generatedColumn).isNull();
    }

    @Test
    void autoIncrementId() {
        InsertQueryBuilder<AutoIncrementIdEntity> builder = new InsertQueryBuilder<>(AutoIncrementIdEntity.class);
        InsertQuery<AutoIncrementIdEntity> query = builder.build();

        assertThat(builder.builder.sourceCode()).isEqualTo(ClasspathResources.text("db-test/insert-query-param-builder-auto-increment-id.java"));

        assertThat(query.sql).isEqualTo("INSERT INTO auto_increment_id_entity (string_field, double_field, enum_field, date_time_field, zoned_date_time_field) VALUES (?, ?, ?, ?, ?)");
        assertThat(query.generatedColumn).isEqualTo("id");
    }

    @Test
    void sequenceId() {
        InsertQuery<SequenceIdEntity> query = new InsertQueryBuilder<>(SequenceIdEntity.class).build();
        assertThat(query.sql).isEqualTo("INSERT INTO sequence_id_entity (id, string_field, long_field) VALUES (seq.NEXTVAL, ?, ?)");
        assertThat(query.generatedColumn).isEqualTo("id");
    }
}
