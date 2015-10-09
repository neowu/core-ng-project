package core.framework.api.module;

import core.framework.impl.inject.BeanFactory;
import core.framework.impl.module.ModuleContext;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author neo
 */
public class DBConfigTest {
    @Test
    public void multipleDB() {
        ModuleContext context = new ModuleContext(new BeanFactory(), null);
        DBConfig defaultDB1 = new DBConfig(context, null);
        DBConfig otherDB1 = new DBConfig(context, "other");
        Assert.assertNotSame(defaultDB1.database, otherDB1.database);

        DBConfig defaultDB2 = new DBConfig(context, null);
        DBConfig otherDB2 = new DBConfig(context, "other");
        Assert.assertSame(defaultDB1.database, defaultDB2.database);
        Assert.assertSame(otherDB1.database, otherDB2.database);
    }
}