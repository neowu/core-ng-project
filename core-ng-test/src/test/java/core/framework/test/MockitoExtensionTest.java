package core.framework.test;

import core.framework.http.HTTPClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class MockitoExtensionTest {
    @Mock
    HTTPClient httpClient;

    @Test
    void inject() {
        assertNotNull(httpClient);
    }
}
