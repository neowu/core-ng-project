package core.framework.internal.redis;

import core.framework.internal.resource.Pool;
import core.framework.internal.resource.PoolItem;
import core.framework.util.Strings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static core.framework.internal.redis.RedisEncodings.decode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
abstract class AbstractRedisOperationTest {
    protected RedisImpl redis;
    @Mock
    Pool<RedisConnection> pool;
    private ByteArrayOutputStream request;
    private PoolItem<RedisConnection> poolItem;

    @BeforeEach
    void createRedis() {
        request = new ByteArrayOutputStream();
        var connection = new RedisConnection();
        connection.outputStream = new RedisOutputStream(request, 512);
        poolItem = new PoolItem<>(connection);
        redis = new RedisImpl(null);
        redis.pool = pool;
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
