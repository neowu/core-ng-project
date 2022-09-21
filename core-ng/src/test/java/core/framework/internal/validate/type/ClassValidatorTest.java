package core.framework.internal.validate.type;


import core.framework.internal.validate.ClassValidator;
import org.junit.jupiter.api.Test;

import java.util.Date;
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

        assertThatThrownBy(() -> new ClassValidator(TestClassNotStatic.class).validate())
            .isInstanceOf(Error.class)
            .hasMessageContaining("class must be static");

        assertThatThrownBy(() -> new ClassValidator(TestClassNotPublic.class).validate())
            .isInstanceOf(Error.class)
            .hasMessageContaining("class must be public concrete");
    }

    @Test
    void validate() {
        var validator = new ClassValidator(TestBean.class);
        validator.allowedValueClasses = Set.of(String.class, Integer.class);
        validator.validate();
    }

    @Test
    void validateMapList() {
        var validator = new ClassValidator(TestBean.class);
        validator.allowedValueClasses = Set.of(String.class);
        assertThatThrownBy(validator::validate)
            .isInstanceOf(Error.class)
            .hasMessageContaining("map list value class is not supported");
    }

    @Test
    void validateWithDateField() {
        assertThatThrownBy(() -> new ClassValidator(TestBeanWithDate.class).validate())
            .isInstanceOf(Error.class)
            .hasMessageContaining("java.util.Date is not supported");
    }

    enum TestEnum {
        A
    }

    public static class TestBean {
        public List<String> listField;
        public Map<String, List<Integer>> mapListField;
        public Map<TestEnum, Integer> mapField;
    }

    static class TestClassNotPublic {
    }

    public static class TestBeanWithDate {
        public Date date;
    }

    public class TestClassNotStatic {
    }
}
