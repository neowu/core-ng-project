package core.framework.api.crypto;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author neo
 */
public class HMACTest {
    @Test
    public void digestByMD5() {
        HMAC hmac = new HMAC("4VPDEtyUE".getBytes(), HMAC.Hash.MD5);
        byte[] bytes = hmac.digest("hello");
        Assert.assertNotNull(bytes);
    }

    @Test
    public void digestBySHA512() {
        HMAC hmac = new HMAC("4VPDEtyUE".getBytes(), HMAC.Hash.SHA512);
        byte[] bytes = hmac.digest("hello");
        Assert.assertNotNull(bytes);
    }

    @Test
    public void generateKey() {
        byte[] key = HMAC.generateKey(HMAC.Hash.SHA512);
        HMAC hmac = new HMAC(key, HMAC.Hash.SHA512);
        byte[] bytes = hmac.digest("hello");
        Assert.assertNotNull(bytes);
    }
}
