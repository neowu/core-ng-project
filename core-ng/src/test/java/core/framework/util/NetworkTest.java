package core.framework.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class NetworkTest {
    @Test
    void localHostAddress() {
        String hostAddress = Network.localHostAddress();
        assertNotNull(hostAddress);
    }

    @Test
    void isLocal() {
        assertTrue(Network.isLocalAddress("127.0.0.1"));
        assertTrue(Network.isLocalAddress("192.168.0.1"));
        assertTrue(Network.isLocalAddress("10.0.0.1"));
        assertTrue(Network.isLocalAddress("::1"));

        assertFalse(Network.isLocalAddress("24.0.0.1"));
    }
}
