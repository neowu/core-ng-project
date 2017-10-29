package core.framework.module;

import core.framework.impl.inject.BeanFactory;
import core.framework.impl.module.ModuleContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author neo
 */
class DBConfigTest {
    private ModuleContext context;

    @BeforeEach
    void createModuleContext() {
        context = new ModuleContext(new BeanFactory(), new TestMockFactory());
    }

    @Test
    void multipleDB() {
        DBConfig defaultDB1 = new DBConfig(context, null);
        DBConfig otherDB1 = new DBConfig(context, "other");
        assertNotSame(defaultDB1.state.database, otherDB1.state.database);

        DBConfig defaultDB2 = new DBConfig(context, null);
        DBConfig otherDB2 = new DBConfig(context, "other");
        assertSame(defaultDB1.state.database, defaultDB2.state.database);
        assertSame(otherDB1.state.database, otherDB2.state.database);
    }
}
