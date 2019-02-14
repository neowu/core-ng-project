package core.framework.internal.cache;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class CacheClassValidatorTest {
    @Test
    void validate() {
        new CacheClassValidator(TestCache.class).validate();
    }

    @Test
    void validateValueType() {
        assertThatThrownBy(() -> new CacheClassValidator(String.class).validate())
                .isInstanceOf(Error.class)
                .hasMessageContaining("class must be bean class");
    }
}
