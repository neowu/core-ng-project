package core.framework.impl.web.session;

import core.framework.api.util.Maps;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class RedisSessionStoreTest {
    RedisSessionStore store;

    @Before
    public void createRedisSessionStore() {
        store = new RedisSessionStore(null);
    }

    @Test
    public void encode() {
        assertEquals("{}", store.encode(Maps.newHashMap()));
    }

    @Test
    public void decode() {
        assertEquals(Maps.newHashMap(), store.decode("{}"));
    }
}