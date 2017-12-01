package core.framework.impl.inject;

import core.framework.inject.Inject;
import core.framework.inject.Named;
import core.framework.util.Types;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        beanFactory.bind(Dependency1.class, null, new Dependency1());
        beanFactory.bind(Types.generic(Dependency2.class, String.class), "dep2", new Dependency2<String>());

        Bean bean = beanFactory.create(Bean.class);
        assertNotNull(bean.dependency1);
        assertNotNull(bean.dependency2);
    }

    @Test
    void bindWithMismatchedType() {
        Error error = assertThrows(Error.class, () -> beanFactory.bind(List.class, null, "instance"));
        assertThat(error.getMessage()).contains("instance type does not match");
    }

    @Test
    void bindGeneric() {
        beanFactory.bind(Types.list(String.class), null, new ArrayList<String>());

        beanFactory.bind(Types.supplier(String.class), null, (Supplier<String>) () -> null);
    }

    static class Dependency1 {
    }

    static class Dependency2<T> {
    }

    public static class Bean {
        @Inject
        Dependency1 dependency1;
        Dependency2<String> dependency2;

        @Inject
        public void setDependency2(@Named("dep2") Dependency2<String> dependency2) {
            this.dependency2 = dependency2;
        }
    }
}
