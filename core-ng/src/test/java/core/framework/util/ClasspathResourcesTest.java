package core.framework.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class ClasspathResourcesTest {
    @Test
    void text() {
        String text = ClasspathResources.text("classpath-resource-test/resource.txt");
        assertEquals("value", text);
    }
}
