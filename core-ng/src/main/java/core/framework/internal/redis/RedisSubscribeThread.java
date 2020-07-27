package core.framework.internal.redis;

import core.framework.util.Threads;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;

import static core.framework.internal.redis.RedisEncodings.decode;

/**
 * @author neo
 */
public class RedisSubscribeThread extends Thread {
    private final Logger logger = LoggerFactory.getLogger(RedisSubscribeThread.class);
    private final RedisImpl redis;
    private final RedisChannelListener listener;
    private final String channel;
    private volatile boolean stop;
    private volatile RedisConnection connection;

    // only used internally, for simplicity, only support very limited use cases
    public RedisSubscribeThread(String name, RedisImpl redis, RedisChannelListener listener, String channel) {
        super(name);
        this.redis = redis;
        this.listener = listener;
        this.channel = channel;
    }

    @Override
    public void run() {
        while (!stop) {
            try (RedisConnection connection = redis.createConnection(0)) {
                this.connection = connection;
                process(connection);
            } catch (Throwable e) {
                this.connection = null;
                if (!stop) {
                    logger.warn("redis subscribe connection failed, retry in 10 seconds", e);
                    Threads.sleepRoughly(Duration.ofSeconds(10));
                }
            }
        }
        logger.info("redis subscribe thread stopped, channel={}", channel);
    }

    void process(RedisConnection connection) throws Exception {
        subscribe(connection);
        listener.onSubscribe();

        while (true) {
            Object value = connection.read();
            if (value instanceof String && "OK".equals(value)) {    // got quit
                break;
            }

            if (value instanceof Object[]) {
                Object[] message = (Object[]) value;
                String kind = decode((byte[]) message[0]);
                if ("message".equals(kind)) {
                    byte[] payload = (byte[]) message[2];
                    listener.onMessage(payload);
                    continue;
                }
            }

            throw new Error("unexpected response, message=" + value);
        }
    }

    private void subscribe(RedisConnection connection) throws IOException {
        connection.writeKeyCommand(Protocol.Command.SUBSCRIBE, channel);
        String kind = decode((byte[]) connection.readArray()[0]);
        if (!"subscribe".equals(kind)) throw new Error("unexpected response, kind=" + kind);
        logger.info("subscribed to redis channel, channel={}", channel);
    }

    public void close() throws IOException {
        logger.info("stopping redis subscribe thread, channel={}", channel);
        stop = true;
        RedisConnection connection = this.connection;
        if (connection != null) {
            connection.writeCommand(Protocol.Command.QUIT); // to simplify by not catching exception, if connection is broken, run loop will cleanup
        }
        interrupt();    // interrupt sleep during failure if needed
    }
}

