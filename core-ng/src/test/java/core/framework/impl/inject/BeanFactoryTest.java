package core.framework.impl.inject;

import core.framework.api.util.Types;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.function.Supplier;

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
        beanFactory.beans.put(new Key(Dependency2.class, "dep2"), new Dependency2());
        beanFactory.beans.put(new Key(Dependency3.class, null), new Dependency3());

        Bean bean = beanFactory.create(Bean.class);
        Assert.assertNotNull(bean.dependency1);
        Assert.assertNotNull(bean.dependency2);
        Assert.assertNotNull(bean.dependency3);
    }

    @Test
    public void bindGeneric() {
        beanFactory.bind(Types.list(String.class), null, new ArrayList<String>());

        beanFactory.bind(Types.supplier(String.class), null, (Supplier<String>) () -> null);
    }

    static class Dependency1 {

    }

    static class Dependency2 {

    }

    static class Dependency3 {

    }

    static class Bean {
        final Dependency3 dependency3;
        @Inject
        Dependency1 dependency1;
        Dependency2 dependency2;

        @Inject
        public Bean(Dependency3 dependency3) {
            this.dependency3 = dependency3;
        }

        @Inject
        public void setDependency2(@Named("dep2") Dependency2 dependency2) {
            this.dependency2 = dependency2;
        }
    }
}