package core.framework.impl.redis;

import core.framework.impl.resource.Pool;
import core.framework.impl.resource.PoolItem;
import core.framework.util.Strings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static core.framework.impl.redis.RedisEncodings.decode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
abstract class AbstractRedisOperationTest {
    protected RedisImpl redis;
    private ByteArrayOutputStream request;
    private PoolItem<RedisConnection> poolItem;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void createRedis() {
        request = new ByteArrayOutputStream();
        var connection = new RedisConnection();
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
        assertThat(decode(request.toByteArray())).isEqualTo(data);
    }

    void response(String data) {
        poolItem.resource.inputStream = new RedisInputStream(new ByteArrayInputStream(Strings.bytes(data)));
    }
}
