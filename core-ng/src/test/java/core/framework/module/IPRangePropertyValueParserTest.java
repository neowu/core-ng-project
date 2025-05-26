package core.framework.module;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class IPRangePropertyValueParserTest {
    @Test
    void parseBlank() {
        assertThat(new IPRangePropertyValueParser("").parse()).isEmpty();
    }

    @Test
    void parseCommaDelimited() {
        assertThat(new IPRangePropertyValueParser("cidr1, cidr2").parse()).containsExactly("cidr1", "cidr2");
        assertThat(new IPRangePropertyValueParser(" cidr1 ").parse()).containsExactly("cidr1");
        assertThat(new IPRangePropertyValueParser("cidr1,cidr2 ").parse()).containsExactly("cidr1", "cidr2");
    }

    @Test
    void parseSemicolonDelimited() {
        assertThat(new IPRangePropertyValueParser("name1: cidr1, cidr2; name2: cidr3,cidr4").parse()).containsExactly("cidr1", "cidr2", "cidr3", "cidr4");
        assertThat(new IPRangePropertyValueParser("name1: cidr1; name2: cidr3; ").parse()).containsExactly("cidr1", "cidr3");
    }

    @Test
    void parseCommaDelimitedWithIPv6() {
        assertThat(new IPRangePropertyValueParser("2001:df0:22b::/48, 2001:df0:442::/47").parse()).containsExactly("2001:df0:22b::/48", "2001:df0:442::/47");
    }

    @Test
    void parseSemicolonDelimitedWithIPv6() {
        assertThat(new IPRangePropertyValueParser("name1: 2001:df0:cc0::/48, 2001:df0:e40::/48; name2: 2001:df0:6a00::/48").parse()).containsExactly("2001:df0:cc0::/48", "2001:df0:e40::/48", "2001:df0:6a00::/48");
        assertThat(new IPRangePropertyValueParser("name1: 2001:df0:a5c0::/48; name2: 2001:df0:c000::/48; ").parse()).containsExactly("2001:df0:a5c0::/48", "2001:df0:c000::/48");
    }
}
