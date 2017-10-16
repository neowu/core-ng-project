package core.framework.impl.search;

import org.junit.jupiter.api.Test;

/**
 * @author neo
 */
class DocumentValidatorTest {
    @Test
    void validate() {
        DocumentValidator<TestDocument> validator = new DocumentValidator<>(TestDocument.class);
        TestDocument document = new TestDocument();
        document.stringField = "value";
        validator.validate(document);
    }
}
