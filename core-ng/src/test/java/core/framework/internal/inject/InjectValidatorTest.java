package core.framework.internal.inject;

import core.framework.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class InjectValidatorTest {
    @Test
    void validate() {
        var bean = new TestBeanWithNotInjectedField();

        var validator = new InjectValidator(bean);
        assertThatThrownBy(validator::validate)
                .isInstanceOf(Error.class)
                .hasMessageContaining("field with @Inject is not bound to any value")
                .hasMessageContaining(".dependency2");
    }

    @Test
    void validateWithCircularReference() {
        var bean = new TestBeanWithCircularReference();
        assertThat(bean.selfDependency).isSameAs(bean.selfDependency.dependency);
        assertThat(bean.dependency1).isSameAs(bean.dependency1.dependency.dependency);
        assertThat(bean.testEnum).isNotNull();

        var validator = new InjectValidator(bean);
        validator.validate();
    }

    public static class TestBeanWithNotInjectedField {
        public List<Object> list = new ArrayList<>();
        @Inject
        Dependency dependency1 = new Dependency();
        @Inject
        Dependency dependency2;

        static class Dependency {
        }
    }

    public static class TestBeanWithCircularReference {
        SelfDependency selfDependency = new SelfDependency();
        Dependency1 dependency1 = new Dependency1();
        TestEnum testEnum = TestEnum.V1;

        TestBeanWithCircularReference() {
            var dependency2 = new Dependency2();
            dependency1.dependency = dependency2;
            dependency2.dependency = dependency1;

            selfDependency.dependency = selfDependency;
        }

        enum TestEnum {
            V1, V2
        }

        static class Dependency1 {
            Dependency2 dependency;
        }

        static class Dependency2 {
            Dependency1 dependency;
        }

        static class SelfDependency {
            SelfDependency dependency;
        }
    }
}
