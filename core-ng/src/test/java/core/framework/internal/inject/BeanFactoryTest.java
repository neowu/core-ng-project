package core.framework.internal.inject;

import core.framework.inject.Inject;
import core.framework.inject.Named;
import core.framework.util.Types;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class BeanFactoryTest {
    private BeanFactory beanFactory;

    @BeforeEach
    void createBeanFactory() {
        beanFactory = new BeanFactory();
    }

    @Test
    void create() {
        Dependency1 dependency1 = new Dependency1();
        Dependency2<String> dependency2 = new Dependency2<>();
        beanFactory.bind(Dependency1.class, null, dependency1);
        beanFactory.bind(Types.generic(Dependency2.class, String.class), "dep2", dependency2);

        Bean bean = beanFactory.create(Bean.class);
        assertThat(bean.dependency1).isSameAs(dependency1);
        assertThat(bean.dependency2).isSameAs(dependency2);
    }

    @Test
    void bindWithMismatchedType() {
        assertThatThrownBy(() -> beanFactory.bind(List.class, null, "instance"))
            .isInstanceOf(Error.class)
            .hasMessageContaining("instance type does not match");
    }

    @Test
    void injectStaticMembers() {
        assertThatThrownBy(() -> beanFactory.create(BeanWithInjectStaticField.class))
            .isInstanceOf(Error.class)
            .hasMessageContaining("static field must not have @Inject");
    }

    @Test
    void bindGeneric() {
        beanFactory.bind(Types.list(String.class), null, new ArrayList<>());

        beanFactory.bind(Types.supplier(String.class), null, (Supplier<String>) () -> null);
    }

    @Test
    void createWithInvalidDependency() {
        assertThatThrownBy(() -> beanFactory.create(BeanWithInvalidDependency.class))
            .isInstanceOf(Error.class)
            .hasMessageContaining("can not resolve dependency");
    }

    static class Dependency1 {
    }

    static class Dependency2<T> {
    }

    public static class Bean {
        @Inject
        Dependency1 dependency1;
        @Inject
        @Named("dep2")
        Dependency2<String> dependency2;
    }

    public static class BeanWithInjectStaticField {
        @SuppressWarnings("PMD.MutableStaticState")     // for invalid case
        @Inject
        static Dependency1 dependency1;
    }

    public static class BeanWithInvalidDependency {
        @Inject
        Dependency1 invalidDependency;
    }
}
