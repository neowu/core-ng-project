package core.framework.impl.web.http;

import org.junit.jupiter.api.Test;

import static core.framework.impl.web.http.CIDR.address;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class CIDRTest {
    @Test
    void matchesAll() {
        var cidr = new CIDR("0.0.0.0/0");

        assertThat(cidr.matches(address("192.168.1.1"))).isTrue();
        assertThat(cidr.matches(address("127.0.0.1"))).isTrue();
        assertThat(cidr.matches(address("10.10.0.1"))).isTrue();
        assertThat(cidr.matches(address("2001:0db8:85a3:0000:0000:8a2e:0370:7334"))).isTrue();

        cidr = new CIDR("::0/0");
        assertThat(cidr.matches(address("192.168.1.1"))).isTrue();
        assertThat(cidr.matches(address("127.0.0.1"))).isTrue();
        assertThat(cidr.matches(address("10.10.0.1"))).isTrue();
        assertThat(cidr.matches(address("2001:0db8:85a3:0000:0000:8a2e:0370:7334"))).isTrue();
    }

    @Test
    void matchesWithIPv4() {
        var cidr = new CIDR("192.168.1.0/24");

        assertThat(cidr.matches(address("192.168.1.1"))).isTrue();
        assertThat(cidr.matches(address("192.168.1.254"))).isTrue();
        assertThat(cidr.matches(address("192.168.2.1"))).isFalse();
        assertThat(cidr.matches(address("192.168.0.1"))).isFalse();

        cidr = new CIDR("192.168.1.1/32");

        assertThat(cidr.matches(address("192.168.1.1"))).isTrue();
        assertThat(cidr.matches(address("192.168.1.2"))).isFalse();
        assertThat(cidr.matches(address("192.168.1.3"))).isFalse();

        cidr = new CIDR("192.168.1.1/31");

        assertThat(cidr.matches(address("192.168.1.0"))).isTrue();
        assertThat(cidr.matches(address("192.168.1.1"))).isTrue();
        assertThat(cidr.matches(address("192.168.1.2"))).isFalse();

        cidr = new CIDR("192.168.1.1/30");

        assertThat(cidr.matches(address("192.168.1.0"))).isTrue();
        assertThat(cidr.matches(address("192.168.1.1"))).isTrue();
        assertThat(cidr.matches(address("192.168.1.2"))).isTrue();
        assertThat(cidr.matches(address("192.168.1.3"))).isTrue();
        assertThat(cidr.matches(address("192.168.1.4"))).isFalse();

        cidr = new CIDR("119.137.52.0/22");

        assertThat(cidr.matches(address("119.137.52.1"))).isTrue();
        assertThat(cidr.matches(address("119.137.53.1"))).isTrue();
        assertThat(cidr.matches(address("119.137.53.254"))).isTrue();
        assertThat(cidr.matches(address("119.137.54.254"))).isTrue();
    }

    @Test
    void matchesWithIPv6() {
        var cidr = new CIDR("2001:0db8:85a3:0000:0000:8a2e:0370:0000/112");

        assertThat(cidr.matches(address("2001:0db8:85a3:0000:0000:8a2e:0370:7334"))).isTrue();
    }
}
