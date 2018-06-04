package core.framework.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author neo
 */
class RandomsTest {
    @Test
    void alphaNumeric() {
        assertThat(Randoms.alphaNumeric(3).length()).isEqualTo(3);
        assertThat(Randoms.alphaNumeric(5).length()).isEqualTo(5);
        assertThat(Randoms.alphaNumeric(10).length()).isEqualTo(10);
    }

    @Test
    void nextDouble() {
        double number = Randoms.nextDouble(8000, 12000);
        assertThat(number).isGreaterThanOrEqualTo(8000).isLessThan(12000);

        number = Randoms.nextDouble(0.8, 1.2);
        assertThat(number).isGreaterThanOrEqualTo(0.8).isLessThan(1.2);
    }

    @Test
    void nextInt() {
        int number = Randoms.nextInt(0, 10);
        assertThat(number).isGreaterThanOrEqualTo(0).isLessThan(10);

        number = Randoms.nextInt(5, 1000);
        assertThat(number).isGreaterThanOrEqualTo(5).isLessThan(1000);
    }
}
