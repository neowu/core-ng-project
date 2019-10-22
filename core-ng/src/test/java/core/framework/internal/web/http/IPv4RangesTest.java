package core.framework.internal.web.http;

import org.junit.jupiter.api.Test;

import java.util.List;

import static core.framework.internal.web.http.IPv4Ranges.address;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class IPv4RangesTest {
    @Test
    void withinRanges() {
        int[] ranges = {1, 1, 2, 3, 5, 8, 20, 30};

        assertThat(IPv4Ranges.withinRanges(ranges, 0)).isFalse();
        assertThat(IPv4Ranges.withinRanges(ranges, 2)).isTrue();
        assertThat(IPv4Ranges.withinRanges(ranges, 3)).isTrue();
        assertThat(IPv4Ranges.withinRanges(ranges, 4)).isFalse();
        assertThat(IPv4Ranges.withinRanges(ranges, 9)).isFalse();
        assertThat(IPv4Ranges.withinRanges(ranges, 22)).isTrue();
        assertThat(IPv4Ranges.withinRanges(ranges, 31)).isFalse();
    }

    @Test
    void mergeRanges() {
        int[][] ranges1 = {{1, 2}, {4, 5}, {5, 9}, {3, 5}, {10, 20}};
        assertThat(IPv4Ranges.mergeRanges(ranges1)).containsExactly(1, 2, 3, 9, 10, 20);

        int[][] ranges2 = {{1, 2}, {10, 20}, {3, 5}, {30, 40}};
        assertThat(IPv4Ranges.mergeRanges(ranges2)).containsExactly(1, 2, 3, 5, 10, 20, 30, 40);
    }

    @Test
    void matchesAll() {
        var ranges = new IPv4Ranges(List.of("0.0.0.0/0"));

        assertThat(ranges.matches(address("192.168.1.1"))).isTrue();
        assertThat(ranges.matches(address("127.0.0.1"))).isTrue();
        assertThat(ranges.matches(address("10.10.0.1"))).isTrue();
    }

    @Test
    void matches() {
        var ranges = new IPv4Ranges(List.of("192.168.1.0/24"));
        assertThat(ranges.matches(address("192.168.1.1"))).isTrue();
        assertThat(ranges.matches(address("192.168.1.254"))).isTrue();
        assertThat(ranges.matches(address("192.168.2.1"))).isFalse();
        assertThat(ranges.matches(address("192.168.0.1"))).isFalse();

        ranges = new IPv4Ranges(List.of("192.168.1.1/32"));
        assertThat(ranges.matches(address("192.168.1.1"))).isTrue();
        assertThat(ranges.matches(address("192.168.1.2"))).isFalse();
        assertThat(ranges.matches(address("192.168.1.3"))).isFalse();

        ranges = new IPv4Ranges(List.of("192.168.1.1/31"));
        assertThat(ranges.matches(address("192.168.1.0"))).isTrue();
        assertThat(ranges.matches(address("192.168.1.1"))).isTrue();
        assertThat(ranges.matches(address("192.168.1.2"))).isFalse();

        ranges = new IPv4Ranges(List.of("192.168.1.1/30"));
        assertThat(ranges.matches(address("192.168.1.0"))).isTrue();
        assertThat(ranges.matches(address("192.168.1.1"))).isTrue();
        assertThat(ranges.matches(address("192.168.1.2"))).isTrue();
        assertThat(ranges.matches(address("192.168.1.3"))).isTrue();
        assertThat(ranges.matches(address("192.168.1.4"))).isFalse();

        ranges = new IPv4Ranges(List.of("119.137.52.0/22"));
        assertThat(ranges.matches(address("119.137.52.1"))).isTrue();
        assertThat(ranges.matches(address("119.137.53.1"))).isTrue();
        assertThat(ranges.matches(address("119.137.53.254"))).isTrue();
        assertThat(ranges.matches(address("119.137.54.254"))).isTrue();

        ranges = new IPv4Ranges(List.of("42.200.0.0/16", "43.224.4.0/22", "43.224.28.0/22"));
        assertThat(ranges.matches(address("42.119.0.1"))).isFalse();
        assertThat(ranges.matches(address("42.200.218.1"))).isTrue();
        assertThat(ranges.matches(address("42.201.218.1"))).isFalse();
        assertThat(ranges.matches(address("43.224.32.1"))).isFalse();
    }

    @Test
    void matchWithEmptyRanges() {
        var ranges = new IPv4Ranges(List.of());
        assertThat(ranges.matches(address("192.168.1.1"))).isFalse();
    }
}
