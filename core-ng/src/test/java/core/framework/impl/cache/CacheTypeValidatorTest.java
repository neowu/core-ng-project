package core.framework.impl.cache;

import core.framework.util.Types;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class CacheTypeValidatorTest {
    @Test
    void validate() {
        new CacheTypeValidator(TestCache.class).validate();
    }

    @Test
    void validateListType() {
        new CacheTypeValidator(Types.list(TestCache.class)).validate();
    }

    @Test
    void validateValueType() {
        assertThatThrownBy(() -> new CacheTypeValidator(String.class).validate())
                .isInstanceOf(Error.class)
                .hasMessageContaining("class must be bean class");
    }
}
