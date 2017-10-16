package core.framework.impl.kafka;

import org.junit.jupiter.api.Test;

/**
 * @author neo
 */
class MessageClassValidatorTest {
    @Test
    void validate() {
        new MessageClassValidator(TestMessage.class).validate();
    }
}
