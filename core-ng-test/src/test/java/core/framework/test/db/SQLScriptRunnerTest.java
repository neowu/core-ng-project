package core.framework.test.db;

import core.framework.db.Database;
import core.framework.inject.Inject;
import core.framework.test.IntegrationTest;
import core.framework.util.ClasspathResources;
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
        var runner = new SQLScriptRunner(database, ClasspathResources.text("db-test/test-script.sql"));
        runner.run();

        Optional<Integer> value = database.selectOne("select int_field from test_entity where id = ?", Integer.class, "sql-runner-test");
        assertThat(value).get().isEqualTo(1);
    }
}
