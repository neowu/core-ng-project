package core.framework.crypto;

import core.framework.util.Strings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author neo
 */
class HMACTest {
    @Test
    void digestBySHA512() {
        HMAC hmac = new HMAC(Strings.bytes("4VPDEtyUE"), HMAC.Hash.SHA512);
        byte[] bytes = hmac.digest(Strings.bytes("hello"));
        assertNotNull(bytes);
    }

    @Test
    void generateKey() {
        byte[] key = HMAC.generateKey(HMAC.Hash.SHA512);
        HMAC hmac = new HMAC(key, HMAC.Hash.SHA512);
        byte[] bytes = hmac.digest(Strings.bytes("hello"));
        assertNotNull(bytes);
    }
}
