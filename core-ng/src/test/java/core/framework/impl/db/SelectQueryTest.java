package core.framework.impl.db;

import core.framework.api.db.Query;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class SelectQueryTest {
    @Test
    public void params() {
        SelectQuery selectQuery = new SelectQuery(AssignedIdEntity.class);
        Query query = new Query();
        query.params = new Object[]{"value"};
        query.limit = 100;
        Object[] params = selectQuery.params(query);

        assertEquals(3, params.length);
        assertEquals("value", params[0]);
        assertEquals("default skip is 0", 0, params[1]);
        assertEquals(100, params[2]);
    }
}