package core.framework.internal.cache;

import core.framework.internal.json.JSONMapper;
import core.framework.internal.redis.RedisChannelListener;
import core.framework.util.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author neo
 */
public class InvalidateLocalCacheMessageListener implements RedisChannelListener {
    private final Logger logger = LoggerFactory.getLogger(InvalidateLocalCacheMessageListener.class);
    private final LocalCacheStore localCache;
    private final JSONMapper<InvalidateLocalCacheMessage> mapper;

    public InvalidateLocalCacheMessageListener(LocalCacheStore localCache, JSONMapper<InvalidateLocalCacheMessage> mapper) {
        this.localCache = localCache;
        this.mapper = mapper;
    }

    @Override
    public void onSubscribe() {
        logger.info("clear local cache");
        localCache.clear();
    }

    @Override
    public void onMessage(byte[] message) throws IOException {
        InvalidateLocalCacheMessage invalidateMessage = mapper.fromJSON(message);
        if (!Network.LOCAL_HOST_ADDRESS.equals(invalidateMessage.clientIP)) {
            logger.info("invalidate local cache, keys={}", invalidateMessage.keys);
            localCache.delete(invalidateMessage.keys.toArray(String[]::new));
        }
    }
}
