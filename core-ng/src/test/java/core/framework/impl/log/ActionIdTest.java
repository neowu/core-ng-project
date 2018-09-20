package core.framework.impl.log;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ActionIdTest {
    @Test
    void next() {
        String id1 = ActionId.next();
        assertThat(id1).hasSize(20);
        String id2 = ActionId.next();
        assertThat(id2).hasSize(20);

        assertThat(id1.substring(5, 8)).as("machine identifier should be same").isEqualTo(id2.substring(5, 8));
    }
}
