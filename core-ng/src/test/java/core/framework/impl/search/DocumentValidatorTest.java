package core.framework.impl.search;

import org.junit.Test;

/**
 * @author neo
 */
public class DocumentValidatorTest {
    @Test
    public void validate() {
        DocumentValidator<TestDocument> validator = new DocumentValidator<>(TestDocument.class);
        TestDocument document = new TestDocument();
        document.stringField = "value";
        validator.validate(document);
    }
}
