package core.framework.module;

import core.framework.impl.inject.BeanFactory;
import core.framework.impl.module.ModuleContext;
import org.junit.Test;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

/**
 * @author neo
 */
public class DBConfigTest {
    @Test
    public void multipleDB() {
        ModuleContext context = new ModuleContext(new BeanFactory(), null);
        DBConfig defaultDB1 = new DBConfig(context, null);
        DBConfig otherDB1 = new DBConfig(context, "other");
        assertNotSame(defaultDB1.state.database, otherDB1.state.database);

        DBConfig defaultDB2 = new DBConfig(context, null);
        DBConfig otherDB2 = new DBConfig(context, "other");
        assertSame(defaultDB1.state.database, defaultDB2.state.database);
        assertSame(otherDB1.state.database, otherDB2.state.database);
    }
}
