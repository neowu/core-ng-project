package core.framework.impl.inject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KeyTest {
    @Test
    void asMapKey() {
        Key stringKey1 = new Key(String.class, null);
        Key stringKey2 = new Key(String.class, null);
        assertThat(stringKey1)
                .isEqualTo(stringKey2)
                .hasSameHashCodeAs(stringKey2);

        Key stringKey3 = new Key(String.class, "name");
        Key stringKey4 = new Key(String.class, "name");
        assertThat(stringKey3)
                .isEqualTo(stringKey4)
                .hasSameHashCodeAs(stringKey4);

        assertThat(new Key(String.class, null)).isNotEqualTo(new Key(int.class, null));
    }
}
