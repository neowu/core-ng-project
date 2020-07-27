package core.framework.internal.web.management;

import core.framework.web.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class CacheControllerTest {
    private CacheController controller;

    @BeforeEach
    void createCacheController() {
        controller = new CacheController(new HashMap<>());
    }

    @Test
    void cache() {
        assertThatThrownBy(() -> controller.cache("notExistingCache"))
                .isInstanceOf(NotFoundException.class);
    }
}
