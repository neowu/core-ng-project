package core.framework.api.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author neo
 */
public class PropertiesTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    private Properties properties;

    @Before
    public void createProperties() {
        properties = new Properties();
    }

    @Test
    public void getEmptyValue() {
        properties.properties.put("key", "");

        Assert.assertFalse(properties.get("key").isPresent());
    }

    @Test
    public void loadNotExistedFile() {
        exception.expect(Error.class);
        exception.expectMessage("can not find");

        properties.load("not-existed-property.properties");
    }
}