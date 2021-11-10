package core.framework.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class NetworkTest {
    @Test
    void localhostAddress() {
        assertThat(Network.LOCAL_HOST_ADDRESS).isNotNull();
    }

    @Test
    void localhostName() {
        assertThat(Network.LOCAL_HOST_NAME).isNotNull();
    }
}
