package core.framework.api.util;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author neo
 */
public class RandomsTest {
    @Test
    public void randomAlphaNumeric() {
        assertThat(Randoms.alphaNumeric(3).length(), equalTo(3));
        assertThat(Randoms.alphaNumeric(5).length(), equalTo(5));
        assertThat(Randoms.alphaNumeric(10).length(), equalTo(10));
    }
}