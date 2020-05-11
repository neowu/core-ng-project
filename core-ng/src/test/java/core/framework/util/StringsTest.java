package core.framework.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class StringsTest {
    @Test
    void stringEquals() {
        assertThat(Strings.equals(null, null)).isTrue();
        assertThat(Strings.equals(null, "")).isFalse();
        assertThat(Strings.equals("", null)).isFalse();
        assertThat(Strings.equals("", "")).isTrue();
    }

    @Test
    void compare() {
        assertThat(Strings.compare(null, "")).isEqualTo(-1);
        assertThat(Strings.compare("b", "a")).isEqualTo(1);
        assertThat(Strings.compare(null, null)).isZero();
    }

    @Test
    void truncate() {
        assertThat(Strings.truncate(null, 10)).isNull();
        assertThat(Strings.truncate("value", 10)).isEqualTo("value");
        assertThat(Strings.truncate("123456789012345", 10)).isEqualTo("1234567890");
    }

    @Test
    void isBlank() {
        assertThat(Strings.isBlank("")).isTrue();
        assertThat(Strings.isBlank(" ")).isTrue();

        assertThat(Strings.isBlank("1")).isFalse();
        assertThat(Strings.isBlank(" 1")).isFalse();
    }

    @Test
    void split() {
        assertThat(Strings.split("", '/')).containsExactly("");
        assertThat(Strings.split("/", '/')).containsExactly("", "");
        assertThat(Strings.split("//", '/')).containsExactly("", "", "");
        assertThat(Strings.split("/1", '/')).containsExactly("", "1");
        assertThat(Strings.split("/1/", '/')).containsExactly("", "1", "");
        assertThat(Strings.split("1/", '/')).containsExactly("1", "");
        assertThat(Strings.split("1/2", '/')).containsExactly("1", "2");
        assertThat(Strings.split("1/2//3", '/')).containsExactly("1", "2", "", "3");
        assertThat(Strings.split("1/2/3", '/')).containsExactly("1", "2", "3");
    }

    @Test
    void format() {
        // ignore surplus arguments
        assertThat(Strings.format("{}, {}, {}", 1, 2, 3, 4)).isEqualTo("1, 2, 3");

        assertThat(Strings.format("{}")).isEqualTo("{}");
        assertThat(Strings.format("{}", (Object[]) null)).isEqualTo("{}");
    }

    @Test
    void strip() {
        assertThat(Strings.strip(null)).isNull();
        assertThat(Strings.strip(" text ")).isEqualTo("text");
    }
}
