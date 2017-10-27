package core.framework.impl.redis;

import core.framework.impl.resource.Pool;
import core.framework.impl.resource.PoolItem;
import core.framework.util.Strings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Duration;

import static core.framework.impl.redis.RedisEncodings.decode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
abstract class AbstractRedisOperationTest {
    RedisImpl redis;
    private ByteArrayOutputStream request;
    private PoolItem<RedisConnection> poolItem;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void createRedis() {
        request = new ByteArrayOutputStream();
        RedisConnection connection = new RedisConnection(null, Duration.ZERO);
        connection.outputStream = new RedisOutputStream(request, 512);
        poolItem = new PoolItem<>(connection);
        redis = new RedisImpl(null);
        redis.pool = (Pool<RedisConnection>) Mockito.mock(Pool.class);
        when(redis.pool.borrowItem()).thenReturn(poolItem);
    }

    @AfterEach
    void verifyPoolItemReturned() {
        verify(redis.pool).returnItem(poolItem);
    }

    void assertRequestEquals(String data) {
        assertEquals(data, decode(request.toByteArray()));
    }

    void response(String data) {
        poolItem.resource.inputStream = new RedisInputStream(new ByteArrayInputStream(Strings.bytes(data)));
    }
}
