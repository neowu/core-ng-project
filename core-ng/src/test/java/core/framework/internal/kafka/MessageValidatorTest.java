package core.framework.internal.kafka;

import org.junit.jupiter.api.Test;

/**
 * @author neo
 */
class MessageValidatorTest {
    @Test
    void validate() {
        var validator = new MessageValidator<>(TestMessage.class);
        var message = new TestMessage();
        message.stringField = "value";
        validator.validate(message);
    }
}
