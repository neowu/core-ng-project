package core.framework.internal.db;

import core.framework.util.ClasspathResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class UpdateQueryBuilderTest {
    private UpdateQueryBuilder<AutoIncrementIdEntity> builder;
    private UpdateQuery<AutoIncrementIdEntity> updateQuery;

    @BeforeEach
    void createUpdateQuery() {
        builder = new UpdateQueryBuilder<>(AutoIncrementIdEntity.class);
        updateQuery = builder.build();
    }

    @Test
    void sourceCode() {
        String sourceCode = builder.builder.sourceCode();
        assertThat(sourceCode).isEqualTo(ClasspathResources.text("db-test/update-query-auto-increment-id.java"));
    }

    @Test
    void update() {
        var entity = new AutoIncrementIdEntity();
        entity.id = 1;
        entity.stringField = "new_value";
        UpdateQuery.Statement statement = updateQuery.update(entity, true, null, null);

        assertThat(statement.sql).isEqualTo("UPDATE auto_increment_id_entity SET string_field = ? WHERE id = ?");
        assertThat(statement.params).hasSize(2).contains(entity.stringField, entity.id);
    }

    @Test
    void updateWithCondition() {
        var entity = new AutoIncrementIdEntity();
        entity.id = 1;
        entity.stringField = "new_value";
        entity.enumField = TestEnum.V2;
        UpdateQuery.Statement statement = updateQuery.update(entity, true, "enum_field = ?", new Object[]{TestEnum.V1});

        assertThat(statement.sql).isEqualTo("UPDATE auto_increment_id_entity SET string_field = ?, enum_field = ? WHERE id = ? AND (enum_field = ?)");
        assertThat(statement.params).hasSize(4).contains(entity.stringField, TestEnum.V2, entity.id, TestEnum.V1);
    }
}
