package core.framework.impl.web.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class WebContextImplTest {
    private WebContextImpl context;

    @BeforeEach
    void createWebContextImpl() {
        context = new WebContextImpl();
        context.initialize(null);
    }

    @AfterEach
    void cleanup() {
        context.cleanup();
    }

    @Test
    void get() {
        context.put("key", "value");

        String value = (String) context.get("key");
        assertThat(value).isEqualTo("value");
    }
}
