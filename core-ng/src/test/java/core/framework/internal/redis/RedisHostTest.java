package core.framework.internal.redis;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class RedisHostTest {
    @Test
    void hostOnly() {
        var host = new RedisHost("cache");
        assertThat(host.host).isEqualTo("cache");
        assertThat(host.port).isEqualTo(RedisHost.DEFAULT_PORT);
    }

    @Test
    void withPort() {
        var host = new RedisHost("proxy:1999");
        assertThat(host.host).isEqualTo("proxy");
        assertThat(host.port).isEqualTo(1999);
    }

    @Test
    void invalidFormat() {
        assertThatThrownBy(() -> new RedisHost("cache:"))
                .isInstanceOf(Error.class)
                .hasMessageContaining("invalid host");

        assertThatThrownBy(() -> new RedisHost(":1999"))
                .isInstanceOf(Error.class)
                .hasMessageContaining("invalid host");

        assertThatThrownBy(() -> new RedisHost("redis:port"))
                .isInstanceOf(Error.class)
                .hasMessageContaining("invalid host");
    }

    @Test
    void convertToString() {
        assertThat(new RedisHost("redis-1").toString()).isEqualTo("redis-1");
        assertThat(new RedisHost("redis-2:6379").toString()).isEqualTo("redis-2");
        assertThat(new RedisHost("redis-3:1999").toString()).isEqualTo("redis-3:1999");
    }
}
