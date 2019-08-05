package core.framework.module;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class IPv4RangePropertyValueParserTest {
    @Test
    void parseBlank() {
        assertThat(new IPv4RangePropertyValueParser("").parse()).isEmpty();
    }

    @Test
    void parseCommaDelimited() {
        assertThat(new IPv4RangePropertyValueParser("cidr1, cidr2").parse()).containsExactly("cidr1", "cidr2");
        assertThat(new IPv4RangePropertyValueParser(" cidr1 ").parse()).containsExactly("cidr1");
        assertThat(new IPv4RangePropertyValueParser("cidr1,cidr2 ").parse()).containsExactly("cidr1", "cidr2");
    }

    @Test
    void parseSemicolonDelimited() {
        assertThat(new IPv4RangePropertyValueParser("name1: cidr1, cidr2; name2: cidr3,cidr4").parse()).containsExactly("cidr1", "cidr2", "cidr3", "cidr4");
        assertThat(new IPv4RangePropertyValueParser("name1: cidr1; name2: cidr3; ").parse()).containsExactly("cidr1", "cidr3");
    }
}
