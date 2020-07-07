package core.framework.internal.redis;

/**
 * @author neo
 */
public interface RedisChannelListener {
    void onSubscribe();

    void onMessage(byte[] message);
}
