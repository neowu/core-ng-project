package core.framework.impl.inject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class KeyTest {
    @Test
    void asMapKey() {
        Key stringKey1 = new Key(String.class, null);
        Key stringKey2 = new Key(String.class, null);
        assertEquals(stringKey1, stringKey2);
        assertEquals(stringKey1.hashCode(), stringKey2.hashCode());

        Key stringKey3 = new Key(String.class, "name");
        Key stringKey4 = new Key(String.class, "name");
        assertEquals(stringKey3, stringKey4);
        assertEquals(stringKey3.hashCode(), stringKey4.hashCode());

        assertNotEquals(new Key(String.class, null), new Key(int.class, null));
    }
}
