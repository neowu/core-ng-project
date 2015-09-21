package core.framework.api.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.UncheckedIOException;

/**
 * @author neo
 */
public class PropertiesTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

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

    @Test
    public void loadNotExistedFile() {
        exception.expect(UncheckedIOException.class);
        exception.expectMessage("not found");

        properties.load("not-existed-property.properties");
    }
}