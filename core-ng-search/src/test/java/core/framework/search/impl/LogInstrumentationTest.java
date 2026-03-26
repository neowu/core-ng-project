package core.framework.search.impl;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LogInstrumentationTest {
    @Test
    void uri() {
        assertThat(LogInstrumentation.uri("/index/_search", Map.of())).isEqualTo("/index/_search");

        assertThat(LogInstrumentation.uri("/index/_search", Map.of("typed_keys", "true"))).isEqualTo("/index/_search?typed_keys=true");
    }
}
