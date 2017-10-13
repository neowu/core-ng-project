package core.framework.crypto;

import core.framework.util.Strings;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @author neo
 */
public class HMACTest {
    @Test
    public void digestByMD5() {
        HMAC hmac = new HMAC(Strings.bytes("4VPDEtyUE"), HMAC.Hash.MD5);
        byte[] bytes = hmac.digest(Strings.bytes("hello"));
        assertNotNull(bytes);
    }

    @Test
    public void digestBySHA512() {
        HMAC hmac = new HMAC(Strings.bytes("4VPDEtyUE"), HMAC.Hash.SHA512);
        byte[] bytes = hmac.digest(Strings.bytes("hello"));
        assertNotNull(bytes);
    }

    @Test
    public void generateKey() {
        byte[] key = HMAC.generateKey(HMAC.Hash.SHA512);
        HMAC hmac = new HMAC(key, HMAC.Hash.SHA512);
        byte[] bytes = hmac.digest(Strings.bytes("hello"));
        assertNotNull(bytes);
    }
}
