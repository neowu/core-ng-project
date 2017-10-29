package core.framework.module;

import core.framework.impl.inject.BeanFactory;
import core.framework.impl.module.MockFactory;
import core.framework.impl.module.ModuleContext;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author neo
 */
class DBConfigTest {
    @Test
    void multipleDB() {
        ModuleContext context = new ModuleContext(new BeanFactory(), new TestMockFactory());
        DBConfig defaultDB1 = new DBConfig(context, null);
        DBConfig otherDB1 = new DBConfig(context, "other");
        assertNotSame(defaultDB1.state.database, otherDB1.state.database);

        DBConfig defaultDB2 = new DBConfig(context, null);
        DBConfig otherDB2 = new DBConfig(context, "other");
        assertSame(defaultDB1.state.database, defaultDB2.state.database);
        assertSame(otherDB1.state.database, otherDB2.state.database);
    }

    private static class TestMockFactory implements MockFactory {
        @Override
        public <T> T create(Class<T> instanceClass, Object... params) {
            return Mockito.mock(instanceClass);
        }
    }
}
