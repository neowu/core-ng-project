package core.framework.internal.validate.type;


import core.framework.internal.validate.ClassValidator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class ClassValidatorTest {
    @Test
    void validateObjectClass() {
        assertThatThrownBy(() -> new ClassValidator(String.class).validate())
            .isInstanceOf(Error.class)
            .hasMessageContaining("class must be bean class");

        assertThatThrownBy(() -> new ClassValidator(int.class).validate())
            .isInstanceOf(Error.class)
            .hasMessageContaining("class must be bean class");

        assertThatThrownBy(() -> new ClassValidator(TestEnum.class).validate())
            .isInstanceOf(Error.class)
            .hasMessageContaining("class must be bean class");
    }

    @Test
    void validate() {
        var validator = new ClassValidator(TestBean.class);
        validator.allowedValueClasses = Set.of(String.class, Integer.class);
        validator.allowChild = true;
        validator.validate();
    }

    @Test
    void validateMapList() {
        var validator = new ClassValidator(TestBean.class);
        validator.allowedValueClasses = Set.of(String.class);
        validator.allowChild = true;
        assertThatThrownBy(validator::validate)
            .isInstanceOf(Error.class)
            .hasMessageContaining("map list value class is not supported");
    }

    enum TestEnum {
        A
    }

    public static class TestBean {
        public List<String> listField;
        public Map<String, List<Integer>> mapListField;
        public Map<TestEnum, Integer> mapField;
    }
}
