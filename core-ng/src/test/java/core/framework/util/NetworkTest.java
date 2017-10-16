package core.framework.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author neo
 */
class NetworkTest {
    @Test
    void localHostAddress() {
        String hostAddress = Network.localHostAddress();
        assertNotNull(hostAddress);
    }
}
