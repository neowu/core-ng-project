package core.framework.util;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UUIDv7Test {
    @Test
    void randomUUID() {
        UUID uuid = UUIDv7.randomUUID();
        assertThat(uuid.version()).isEqualTo(7);
    }
}
