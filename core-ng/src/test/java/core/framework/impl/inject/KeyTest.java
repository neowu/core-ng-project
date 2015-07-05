package core.framework.impl.inject;

import org.junit.Assert;
import org.junit.Test;

public class KeyTest {
    @Test
    public void asMapKey() {
        Key stringKey1 = new Key(String.class, null);
        Key stringKey2 = new Key(String.class, null);
        Assert.assertEquals(stringKey1, stringKey2);
        Assert.assertEquals(stringKey1.hashCode(), stringKey2.hashCode());

        Key stringKey3 = new Key(String.class, "name");
        Key stringKey4 = new Key(String.class, "name");
        Assert.assertEquals(stringKey3, stringKey4);
        Assert.assertEquals(stringKey3.hashCode(), stringKey4.hashCode());

        Assert.assertNotEquals(new Key(String.class, null), new Key(int.class, null));
    }
}