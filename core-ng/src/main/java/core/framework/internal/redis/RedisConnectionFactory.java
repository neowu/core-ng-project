package core.framework.internal.redis;

import core.framework.internal.resource.Pool;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.function.Supplier;

import static core.framework.internal.redis.Protocol.Command.AUTH;

/**
 * @author neo
 */
class RedisConnectionFactory implements Supplier<RedisConnection> {
    RedisHost host;
    String password;
    int timeoutInMs = (int) Duration.ofSeconds(5).toMillis();

    @Override
    public RedisConnection get() {
        return create(timeoutInMs);
    }

    RedisConnection create(int timeoutInMs) {
        if (host == null) throw new Error("redis host must not be null");
        var connection = new RedisConnection(); // this won't throw exception
        try {
            connection.connect(host.host, host.port, timeoutInMs);
            if (password != null) {
                connection.writeKeyCommand(AUTH, password);
                connection.readSimpleString();
            }
            return connection;
        } catch (RedisException e) {    // redis throws error (WRONGPASS) if AUTH failed
            Pool.closeQuietly(connection);
            throw e;
        } catch (IOException e) {
            Pool.closeQuietly(connection);
            throw new UncheckedIOException(e);
        }
    }
}
