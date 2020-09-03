package core.framework.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ASCIITest {
    @Test
    void isDigit() {
        assertThat(ASCII.isDigit('0')).isTrue();
        assertThat(ASCII.isDigit('9')).isTrue();
        assertThat(ASCII.isDigit('!')).isFalse();
        assertThat(ASCII.isDigit('a')).isFalse();
        assertThat(ASCII.isDigit('z')).isFalse();
        assertThat(ASCII.isDigit('A')).isFalse();
        assertThat(ASCII.isDigit('Z')).isFalse();
    }

    @Test
    void isLetter() {
        assertThat(ASCII.isLetter('0')).isFalse();
        assertThat(ASCII.isLetter('9')).isFalse();
        assertThat(ASCII.isLetter('@')).isFalse();
        assertThat(ASCII.isLetter('a')).isTrue();
        assertThat(ASCII.isLetter('z')).isTrue();
        assertThat(ASCII.isLetter('A')).isTrue();
        assertThat(ASCII.isLetter('Z')).isTrue();
    }

    @Test
    void toUpperCase() {
        assertThat(ASCII.toUpperCase(null)).isNull();
        assertThat(ASCII.toUpperCase("text")).isEqualTo("TEXT");
        assertThat(ASCII.toUpperCase("tExt")).isEqualTo("TEXT");
        assertThat(ASCII.toUpperCase("TEXT")).isEqualTo("TEXT");
        assertThat(ASCII.toUpperCase("01239_-")).isEqualTo("01239_-");
    }

    @Test
    void toLowerCase() {
        assertThat(ASCII.toLowerCase(null)).isNull();
        assertThat(ASCII.toLowerCase("text")).isEqualTo("text");
        assertThat(ASCII.toLowerCase("tExt")).isEqualTo("text");
        assertThat(ASCII.toLowerCase("TEXT")).isEqualTo("text");
        assertThat(ASCII.toLowerCase("01239_-")).isEqualTo("01239_-");
    }

    @Test
    void toUpperCaseChar() {
        assertThat(ASCII.toUpperCase('-')).isEqualTo('-');
        assertThat(ASCII.toUpperCase('a')).isEqualTo('A');
        assertThat(ASCII.toUpperCase('z')).isEqualTo('Z');
        assertThat(ASCII.toUpperCase('0')).isEqualTo('0');
        assertThat(ASCII.toUpperCase('9')).isEqualTo('9');
        assertThat(ASCII.toUpperCase('A')).isEqualTo('A');
        assertThat(ASCII.toUpperCase('Z')).isEqualTo('Z');
    }

    @Test
    void toLowerCaseChar() {
        assertThat(ASCII.toLowerCase('-')).isEqualTo('-');
        assertThat(ASCII.toLowerCase('a')).isEqualTo('a');
        assertThat(ASCII.toLowerCase('z')).isEqualTo('z');
        assertThat(ASCII.toLowerCase('0')).isEqualTo('0');
        assertThat(ASCII.toLowerCase('9')).isEqualTo('9');
        assertThat(ASCII.toLowerCase('A')).isEqualTo('a');
        assertThat(ASCII.toLowerCase('Z')).isEqualTo('z');
    }
}
