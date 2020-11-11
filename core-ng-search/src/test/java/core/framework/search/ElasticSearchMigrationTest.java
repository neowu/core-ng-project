package core.framework.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class ElasticSearchMigrationTest {
    private ElasticSearchMigration migration;

    @BeforeEach
    void createElasticSearchMigration() {
        migration = new ElasticSearchMigration("migration-test/sys.properties");
    }

    @Test
    void migrate() {
        migration.migrate(search -> {
        });

        assertThatThrownBy(() -> migration.migrate(search -> {
            throw new RuntimeException("migration error");
        })).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("migration");
    }
}
