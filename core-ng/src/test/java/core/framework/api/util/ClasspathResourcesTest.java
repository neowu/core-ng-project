package core.framework.api.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author neo
 */
public class ClasspathResourcesTest {
    @Test
    public void text() {
        String text = ClasspathResources.text("classpath-resource-test/resource.txt");
        Assert.assertEquals("value", text);
    }
}