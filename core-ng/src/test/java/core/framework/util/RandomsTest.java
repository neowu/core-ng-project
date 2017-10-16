package core.framework.util;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author neo
 */
class RandomsTest {
    @Test
    void randomAlphaNumeric() {
        assertThat(Randoms.alphaNumeric(3).length(), equalTo(3));
        assertThat(Randoms.alphaNumeric(5).length(), equalTo(5));
        assertThat(Randoms.alphaNumeric(10).length(), equalTo(10));
    }

    @Test
    void randomNumber() {
        double number = Randoms.number(8000, 12000);
        assertTrue(number >= 8000 && number < 12000);

        number = Randoms.number(0.8, 1.2);
        assertTrue(number >= 0.8 && number < 1.2);
    }
}
