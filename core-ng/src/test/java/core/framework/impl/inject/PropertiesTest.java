package core.framework.impl.inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author neo
 */
public class PropertiesTest {
    Properties properties;

    @Before
    public void createProperties() {
        properties = new Properties();
    }

    @Test
    public void getEmptyValue() {
        properties.properties.put("key", "");

        Assert.assertFalse(properties.get("key").isPresent());
    }
}