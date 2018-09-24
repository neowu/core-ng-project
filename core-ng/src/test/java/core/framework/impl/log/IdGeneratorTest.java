package core.framework.impl.log;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class IdGeneratorTest {
    private IdGenerator generator;

    @BeforeEach
    void createIdGenerator() {
        generator = new IdGenerator();
    }

    @Test
    void next() {
        Instant now = Instant.ofEpochMilli(1);

        String id1 = generator.next(now);
        assertThat(id1).hasSize(20).startsWith("0000000001");
        String id2 = generator.next(now);
        assertThat(id2).hasSize(20).startsWith("0000000001");

        assertThat(id1.substring(5, 8)).as("machine identifier should be same").isEqualTo(id2.substring(5, 8));
    }
}
