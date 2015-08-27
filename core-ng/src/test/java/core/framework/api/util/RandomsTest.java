package core.framework.api.util;

import org.junit.Assert;
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

    @Test
    public void randomNumber() {
        double number = Randoms.number(8000, 12000);
        Assert.assertTrue(number >= 8000 && number < 12000);

        number = Randoms.number(0.8, 1.2);
        Assert.assertTrue(number >= 0.8 && number < 1.2);
    }
}