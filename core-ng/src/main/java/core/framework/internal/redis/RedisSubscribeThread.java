package core.framework.internal.redis;

import core.framework.util.Threads;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
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
            try {
                process();
            } catch (IOException | UncheckedIOException e) {
                if (!stop) {
                    logger.warn("redis subscribe connection failed, retry in 10 seconds", e);
                    Threads.sleepRoughly(Duration.ofSeconds(10));
                    closeConnection();
                }
            }
        }
        closeConnection();
        logger.info("redis subscribe thread stopped, channel={}", channel);
    }

    void process() throws IOException {
        subscribe();
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

    private void subscribe() throws IOException {
        connection = redis.createConnection(0);
        connection.writeKeyCommand(Protocol.Command.SUBSCRIBE, channel);
        connection.flush();
        String kind = decode((byte[]) connection.readArray()[0]);
        if (!"subscribe".equals(kind)) throw new Error("unexpected response, kind=" + kind);
        logger.info("subscribed to redis channel, channel={}", channel);
    }

    private void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (IOException e) {
            logger.warn("failed to close redis connection", e);
        }
    }

    public void close() {
        logger.info("stopping redis subscribe thread, channel={}", channel);
        stop = true;
        if (connection != null) {
            try {
                connection.writeKeysCommand(Protocol.Command.QUIT);
            } catch (IOException e) {
                logger.warn("failed to send quit command", e);
                closeConnection();
            }
        }
    }
}

