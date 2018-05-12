package core.framework.search.impl;

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
