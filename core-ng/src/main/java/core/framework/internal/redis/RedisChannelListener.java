package core.framework.internal.redis;

/**
 * @author neo
 */
public interface RedisChannelListener {
    void onSubscribe() throws Exception;

    void onMessage(byte[] message) throws Exception;
}
