package core.framework.internal.stat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CounterTest {
    private Counter counter;

    @BeforeEach
    void createCounter() {
        counter = new Counter();
    }

    @Test
    void maxCount() {
        counter.increase();
        counter.increase();
        counter.decrease();
        assertThat(counter.max()).isEqualTo(2);
        assertThat(counter.max()).isEqualTo(1);
    }
}
