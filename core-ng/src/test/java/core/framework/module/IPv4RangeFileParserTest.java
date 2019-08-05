package core.framework.module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class IPv4RangeFileParserTest {
    private IPv4RangeFileParser parser;

    @BeforeEach
    void createIPv4RangeFileParser() {
        parser = new IPv4RangeFileParser("ip-range-test/cidrs.txt");
    }

    @Test
    void parse() {
        List<String> cidrs = parser.parse();
        assertThat(cidrs).hasSize(12).contains("104.44.236.208/30");
    }
}
