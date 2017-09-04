package core.framework.impl.db.dialect;

import core.framework.api.util.Lists;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class MySQLDialectTest {
    @Test
    public void fetchParams() {
        MySQLDialect dialect = new MySQLDialect(null, null);
        Object[] params = dialect.fetchParams(Lists.newArrayList("value"), null, 100);

        assertEquals(3, params.length);
        assertEquals("value", params[0]);
        assertEquals("default skip should be 0", 0, params[1]);
        assertEquals(100, params[2]);
    }
}
