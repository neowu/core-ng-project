package core.framework.impl.kafka;

import org.junit.Test;

/**
 * @author neo
 */
public class MessageClassValidatorTest {
    @Test
    public void validate() {
        new MessageClassValidator(TestMessage.class).validate();
    }
}
