package core.framework.module;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class AllowCIDRParserTest {
    @Test
    void parseBlank() {
        assertThat(new AllowCIDRParser("").parse()).isEmpty();
    }

    @Test
    void parseCommaDelimited() {
        assertThat(new AllowCIDRParser("cidr1, cidr2").parse()).containsExactly("cidr1", "cidr2");
        assertThat(new AllowCIDRParser(" cidr1 ").parse()).containsExactly("cidr1");
        assertThat(new AllowCIDRParser("cidr1,cidr2 ").parse()).containsExactly("cidr1", "cidr2");
    }

    @Test
    void parseSemicolonDelimited() {
        assertThat(new AllowCIDRParser("name1: cidr1, cidr2; name2: cidr3,cidr4").parse()).containsExactly("cidr1", "cidr2", "cidr3", "cidr4");
        assertThat(new AllowCIDRParser("name1: cidr1; name2: cidr3; ").parse()).containsExactly("cidr1", "cidr3");
    }
}
