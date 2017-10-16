package core.framework.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class HashTest {
    @Test
    void md5Hex() {  // use linux md5sum or Mac md5 to verify
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", Hash.md5Hex(""));
        assertEquals("26b17225b626fb9238849fd60eabdf60", Hash.md5Hex("+"));
        assertEquals("202cb962ac59075b964b07152d234b70", Hash.md5Hex("123"));
    }

    @Test
    void sha1Hex() {     // verify by: echo -n "123" | sha1sum
        assertEquals("da39a3ee5e6b4b0d3255bfef95601890afd80709", Hash.sha1Hex(""));
        assertEquals("a979ef10cc6f6a36df6b8a323307ee3bb2e2db9c", Hash.sha1Hex("+"));
        assertEquals("40bd001563085fc35165329ea1ff5c5ecbdbbeef", Hash.sha1Hex("123"));
    }

    @Test
    void sha256Hex() {     // verify by: echo -n "123" | sha1sum
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", Hash.sha256Hex(""));
        assertEquals("a318c24216defe206feeb73ef5be00033fa9c4a74d0b967f6532a26ca5906d3b", Hash.sha256Hex("+"));
        assertEquals("a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3", Hash.sha256Hex("123"));
    }
}
