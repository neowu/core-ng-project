package core.framework.crypto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class HashTest {
    @Test
    void md5Hex() {  // use linux md5sum or Mac md5 to verify
        assertThat(Hash.md5Hex("")).isEqualTo("d41d8cd98f00b204e9800998ecf8427e");
        assertThat(Hash.md5Hex("+")).isEqualTo("26b17225b626fb9238849fd60eabdf60");
        assertThat(Hash.md5Hex("123")).isEqualTo("202cb962ac59075b964b07152d234b70");
    }

    @Test
    void sha256Hex() {     // verify by: echo -n "123" | sha256sum
        assertThat(Hash.sha256Hex("")).isEqualTo("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
        assertThat(Hash.sha256Hex("+")).isEqualTo("a318c24216defe206feeb73ef5be00033fa9c4a74d0b967f6532a26ca5906d3b");
        assertThat(Hash.sha256Hex("123")).isEqualTo("a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3");
    }

    @Test
    void sha512Hex() {
        assertThat(Hash.sha512Hex("123"))
            .isEqualTo("3c9909afec25354d551dae21590bb26e38d53f2173b8d3dc3eee4c047e7ab1c1eb8b85103e3be7ba613b31bb5c9c36214dc9f14a42fd7a2fdb84856bca5c44c2");
    }
}
