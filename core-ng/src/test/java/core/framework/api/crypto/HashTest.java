package core.framework.api.crypto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class HashTest {
    @Test
    public void md5Hex() {  // use linux md5sum or Mac md5 to verify
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", Hash.md5Hex(""));
        assertEquals("202cb962ac59075b964b07152d234b70", Hash.md5Hex("123"));
    }
}