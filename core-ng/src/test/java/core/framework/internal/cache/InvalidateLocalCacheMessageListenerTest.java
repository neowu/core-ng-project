package core.framework.internal.cache;

import core.framework.json.JSON;
import core.framework.util.Network;
import core.framework.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * @author neo
 */
class InvalidateLocalCacheMessageListenerTest {
    private InvalidateLocalCacheMessageListener listener;
    private LocalCacheStore cacheStore;

    @BeforeEach
    void createInvalidateLocalCacheMessageListener() {
        cacheStore = mock(LocalCacheStore.class);
        listener = new InvalidateLocalCacheMessageListener(cacheStore);
    }

    @Test
    void onSubscribe() {
        listener.onSubscribe();

        verify(cacheStore).clear();
    }

    @Test
    void onMessage() throws IOException {
        var message = new InvalidateLocalCacheMessage();
        message.clientIP = "remoteIP";
        message.keys = List.of("key1", "key2");
        listener.onMessage(Strings.bytes(JSON.toJSON(message)));

        verify(cacheStore).delete("key1", "key2");
    }

    @Test
    void onMessageWithSameClientIP() throws IOException {
        var message = new InvalidateLocalCacheMessage();
        message.clientIP = Network.LOCAL_HOST_ADDRESS;
        message.keys = List.of("key1");
        listener.onMessage(Strings.bytes(JSON.toJSON(message)));

        verify(cacheStore, never()).delete("key1");
    }
}
