package core.framework.web;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class CookieSpecTest {
    @Test
    void asMapKey() {
        var spec1 = new CookieSpec("name").domain("example.com").path("/");
        var spec2 = new CookieSpec("name").domain("example.com").path("/").maxAge(Duration.ofSeconds(30));

        assertThat(spec1).isEqualTo(spec2)
                         .hasSameHashCodeAs(spec2);
    }
}
