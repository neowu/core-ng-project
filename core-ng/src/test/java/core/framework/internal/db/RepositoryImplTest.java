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
    void updatedResults() {
        boolean[] results = new boolean[3];
        assertThat(repository.batchResults(new int[]{Statement.SUCCESS_NO_INFO, 0, Statement.SUCCESS_NO_INFO}, results))
            .isEqualTo(2);
        assertThat(results).contains(true, false, true);

        results = new boolean[2];
        assertThat(repository.batchResults(new int[]{1, 1}, results)).isEqualTo(2);
        assertThat(results).contains(true, true);

        // for upsert, affected rows = 2 if updated
        results = new boolean[2];
        assertThat(repository.batchResults(new int[]{2, 1}, results)).isEqualTo(2);
        assertThat(results).contains(false, true);
    }
}
