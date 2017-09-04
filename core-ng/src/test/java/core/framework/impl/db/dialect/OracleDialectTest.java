package core.framework.impl.db.dialect;

import core.framework.api.util.Lists;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class OracleDialectTest {
    @Test
    public void fetchParams() {
        OracleDialect dialect = new OracleDialect(null, null);

        Object[] params = dialect.fetchParams(Lists.newArrayList("value"), null, 10);
        assertEquals(3, params.length);
        assertEquals("value", params[0]);
        assertEquals("to row num should be 10", 10, params[1]);
        assertEquals("from row num should be 1", 1, params[2]);

        params = dialect.fetchParams(Lists.newArrayList(), 5, 10);
        assertEquals(2, params.length);
        assertEquals("to row num should be 15", 15, params[0]);
        assertEquals("from row num should be 6", 6, params[1]);
    }
}
