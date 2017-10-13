package core.framework.impl.search;

import org.junit.Test;

/**
 * @author neo
 */
public class DocumentClassValidatorTest {
    @Test
    public void validate() {
        new DocumentClassValidator(TestDocument.class).validate();
    }
}
