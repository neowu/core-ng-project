package core.framework.impl.db.dialect;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class MySQLDialectTest {
    @Test
    void fetchParams() {
        MySQLDialect dialect = new MySQLDialect(null, null);
        Object[] params = dialect.fetchParams(List.of("value"), null, 100);

        assertEquals(3, params.length);
        assertEquals("value", params[0]);
        assertEquals(0, params[1], "default skip should be 0");
        assertEquals(100, params[2]);
    }
}
