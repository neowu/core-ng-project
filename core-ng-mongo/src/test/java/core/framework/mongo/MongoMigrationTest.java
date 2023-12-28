package core.framework.mongo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class MongoMigrationTest {
    private MongoMigration migration;

    @BeforeEach
    void createMongoMigration() {
        migration = new MongoMigration("migration-test/sys.properties");
    }

    @Test
    void migrate() {
        assertThatThrownBy(() -> migration.migrate(mongo -> {
            throw new RuntimeException("migration error");
        })).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("migration");
    }

    @Test
    void requiredProperty() {
        assertThat(migration.requiredProperty("sys.mongo.uri")).isNotNull();
    }
}
