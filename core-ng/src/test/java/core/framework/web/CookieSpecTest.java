package core.framework.web;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class CookieSpecTest {
    @Test
    void equals() {
        CookieSpec spec1 = new CookieSpec("name").domain("example.com").path("/");
        CookieSpec spec2 = new CookieSpec("name").domain("example.com").path("/").maxAge(Duration.ofSeconds(30));

        assertThat(spec1).isEqualTo(spec2);
        assertThat(spec1).hasSameHashCodeAs(spec2);
    }
}
