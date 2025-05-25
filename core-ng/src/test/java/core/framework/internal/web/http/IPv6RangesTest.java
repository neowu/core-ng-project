package core.framework.internal.web.http;

import org.junit.jupiter.api.Test;

import java.util.List;

import static core.framework.internal.web.http.IPv4Ranges.address;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class IPv6RangesTest {
    @Test
    void matches() {
        var ranges = new IPv6Ranges(List.of("2001:db8::/32"));
        assertTrue(ranges.matches(address("2001:db8::1")));
        assertTrue(ranges.matches(address("2001:db8::ffff")));
        assertFalse(ranges.matches(address("2001:db9::1")));
        assertFalse(ranges.matches(address("2001:db7::1")));

        ranges = new IPv6Ranges(List.of("2001:db8::1/128"));
        assertTrue(ranges.matches(address("2001:db8::1")));
        assertFalse(ranges.matches(address("2001:db8::2")));
        assertFalse(ranges.matches(address("2001:db8::3")));

        ranges = new IPv6Ranges(List.of("2001:db8::/64"));
        assertTrue(ranges.matches(address("2001:db8::0")));
        assertTrue(ranges.matches(address("2001:db8::1")));
        assertTrue(ranges.matches(address("2001:db8::ffff")));
        assertTrue(ranges.matches(address("2001:db8::ffff:ffff")));
        assertTrue(ranges.matches(address("2001:0db8::ffff:ffff:ffff:ffff")));
        assertFalse(ranges.matches(address("2001:db9::0")));

        ranges = new IPv6Ranges(List.of("2001::/16"));
        assertTrue(ranges.matches(address("2001:db8::0")));
        assertTrue(ranges.matches(address("2001:db8::ffff")));
        assertTrue(ranges.matches(address("2001:db9::0")));
        assertFalse(ranges.matches(address("2002::0")));
    }

    @Test
    void matchesWithMultipleRanges() {
        var ranges = new IPv6Ranges(List.of("2001:db8::/32", "2001:db9::/32", "2001:dba::/32"));
        assertTrue(ranges.matches(address("2001:db8::1")));
        assertTrue(ranges.matches(address("2001:db9::1")));
        assertTrue(ranges.matches(address("2001:dba::1")));
        assertFalse(ranges.matches(address("2001:dbb::1")));
        assertFalse(ranges.matches(address("2001:db7::1")));
    }

    @Test
    void matchesWithOverlappingRanges() {
        var ranges = new IPv6Ranges(List.of("2001:db8::/48", "2001:db8:1::/48"));
        assertTrue(ranges.matches(address("2001:db8::1")));
        assertTrue(ranges.matches(address("2001:db8:1::1")));
        assertFalse(ranges.matches(address("2001:db8:2::1")));
    }

    @Test
    void matchesWithEmptyRanges() {
        var ranges = new IPv6Ranges(List.of());
        assertFalse(ranges.matches(address("2001:db8::1")));
    }

    @Test
    void matchesAll() {
        var ranges = new IPv6Ranges(List.of("::/0"));
        assertThat(ranges.matches(address("2001:db8::1"))).isTrue();
        assertThat(ranges.matches(address("::1"))).isTrue();
        assertTrue(ranges.matches(address("fe80::1")));
    }

    @Test
    void mergeRanges() {
        IPv6Ranges.LongLong[][] ranges1 = {{longLong(1), longLong(2)},
            {longLong(4), longLong(5)},
            {longLong(5), longLong(9)},
            {longLong(3), longLong(5)},
            {longLong(10), longLong(20)}};
        assertThat(IPv6Ranges.mergeRanges(ranges1)).containsExactly(longLong(1), longLong(2), longLong(3), longLong(9), longLong(10), longLong(20));

        IPv6Ranges.LongLong[][] ranges2 = {{longLong(1), longLong(2)},
            {longLong(10), longLong(20)},
            {longLong(3), longLong(5)},
            {longLong(30), longLong(40)}};
        assertThat(IPv6Ranges.mergeRanges(ranges2)).containsExactly(longLong(1), longLong(2), longLong(3), longLong(5), longLong(10), longLong(20), longLong(30), longLong(40));
    }

    IPv6Ranges.LongLong longLong(long value) {
        return new IPv6Ranges.LongLong(value, value);
    }
}
