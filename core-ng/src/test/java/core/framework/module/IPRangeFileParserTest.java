package core.framework.module;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class IPRangeFileParserTest {
    @Test
    void parse() {
        var parser = new IPRangeFileParser("ip-range-test/ipv4-cidrs.txt");
        List<String> cidrs = parser.parse();
        assertThat(cidrs).hasSize(12).contains("104.44.236.208/30");
    }

    @Test
    void parseIPv6() {
        var parser = new IPRangeFileParser("ip-range-test/ipv6-cidrs.txt");
        List<String> cidrs = parser.parse();
        assertThat(cidrs).hasSize(14).contains("2001:def::/48");
    }
}
