package core.framework.test;

import core.framework.test.module.AbstractTestModule;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
public class OverrideBeanTest extends AbstractTestModule {
    @Override
    protected void initialize() {
        TestBean bean = overrideBinding(TestBean.class, new TestBean("overrideBean1"));
        assertThat(bean.name).isEqualTo("overrideBean1");

        bean = bind(TestBean.class, new TestBean("bean"));
        assertThat(bean.name).isEqualTo("overrideBean1");

        bean = overrideBinding(TestBean.class, "name", new TestBean("overrideBean2"));
        assertThat(bean.name).isEqualTo("overrideBean2");

        bean = bind(TestBean.class, "name", new TestBean("bean"));
        assertThat(bean.name).isEqualTo("overrideBean2");
    }

    public static class TestBean {
        public final String name;

        TestBean(String name) {
            this.name = name;
        }
    }
}
