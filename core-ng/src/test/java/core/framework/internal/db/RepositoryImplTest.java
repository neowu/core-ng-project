package core.framework.internal.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class RepositoryImplTest {
    private RepositoryImpl<AssignedIdEntity> repository;

    @BeforeEach
    void createRepository() {
        repository = new RepositoryImpl<>(null, AssignedIdEntity.class);
    }

    @Test
    void insertedRows() {
        assertThat(repository.insertedRows(new int[]{Statement.SUCCESS_NO_INFO, 0, Statement.SUCCESS_NO_INFO})).isEqualTo(2);
        assertThat(repository.insertedRows(new int[]{1, 1})).isEqualTo(2);
    }
}
