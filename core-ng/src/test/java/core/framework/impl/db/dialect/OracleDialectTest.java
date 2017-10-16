package core.framework.impl.db.dialect;

import core.framework.util.Lists;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class OracleDialectTest {
    @Test
    void fetchParams() {
        OracleDialect dialect = new OracleDialect(null, null);

        Object[] params = dialect.fetchParams(Lists.newArrayList("value"), null, 10);
        assertEquals(3, params.length);
        assertEquals("value", params[0]);
        assertEquals(10, params[1], "to row num should be 10");
        assertEquals(1, params[2], "from row num should be 1");

        params = dialect.fetchParams(Lists.newArrayList(), 5, 10);
        assertEquals(2, params.length);
        assertEquals(15, params[0], "to row num should be 15");
        assertEquals(6, params[1], "from row num should be 6");
    }
}
