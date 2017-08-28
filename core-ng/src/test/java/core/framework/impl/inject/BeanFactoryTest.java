package core.framework.impl.inject;

import core.framework.api.util.Types;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.function.Supplier;

import static org.junit.Assert.assertNotNull;

/**
 * @author neo
 */
public class BeanFactoryTest {
    private BeanFactory beanFactory;

    @Before
    public void createBeanFactory() {
        beanFactory = new BeanFactory();
    }

    @Test
    public void create() {
        beanFactory.beans.put(new Key(Dependency1.class, null), new Dependency1());
        beanFactory.beans.put(new Key(Types.generic(Dependency2.class, String.class), "dep2"), new Dependency2<String>());
        beanFactory.beans.put(new Key(Dependency3.class, null), new Dependency3());

        Bean bean = beanFactory.create(Bean.class);
        assertNotNull(bean.dependency1);
        assertNotNull(bean.dependency2);
        assertNotNull(bean.dependency3);
    }

    @Test
    public void bindGeneric() {
        beanFactory.bind(Types.list(String.class), null, new ArrayList<String>());

        beanFactory.bind(Types.supplier(String.class), null, (Supplier<String>) () -> null);
    }

    static class Dependency1 {

    }

    static class Dependency2<T> {

    }

    static class Dependency3 {

    }

    static class Bean {
        final Dependency3 dependency3;
        @Inject
        Dependency1 dependency1;
        Dependency2<String> dependency2;

        @Inject
        Bean(Dependency3 dependency3) {
            this.dependency3 = dependency3;
        }

        @Inject
        public void setDependency2(@Named("dep2") Dependency2<String> dependency2) {
            this.dependency2 = dependency2;
        }
    }
}
