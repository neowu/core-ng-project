package core.framework.test.db;

import core.framework.db.Database;
import core.framework.inject.Inject;
import core.framework.test.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class SQLScriptRunnerTest extends IntegrationTest {
    @Inject
    Database database;

    @Test
    void run() {
        String script = String.join("\n", "CREATE INDEX idx_test_entity_int_field ON test_entity (int_field);",
                "CREATE INDEX idx_test_entity_string_field ON test_entity (string_field);",
                "INSERT INTO test_entity (id, int_field) VALUES ('sql-runner-test', 1);");

        var runner = new SQLScriptRunner(database, script);
        runner.run();

        Optional<Integer> value = database.selectOne("select int_field from test_entity where id = ?", Integer.class, "sql-runner-test");
        assertThat(value).get().isEqualTo(1);
    }
}
