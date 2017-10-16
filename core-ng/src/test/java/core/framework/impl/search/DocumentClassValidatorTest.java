package core.framework.impl.search;

import org.junit.jupiter.api.Test;

/**
 * @author neo
 */
class DocumentClassValidatorTest {
    @Test
    void validate() {
        new DocumentClassValidator(TestDocument.class).validate();
    }
}
