package core.framework.module;

import core.framework.db.Repository;
import core.framework.impl.module.Config;
import core.framework.impl.module.ModuleContext;
import core.framework.test.db.EntitySchemaGenerator;
import core.framework.test.db.SQLScriptRunner;
import core.framework.test.module.TestModuleContext;
import core.framework.util.ClasspathResources;
import core.framework.util.Types;

import java.util.List;

/**
 * @author neo
 */
public final class InitDBConfig extends Config {
    private TestModuleContext context;
    private String name;
    private DBConfig config;

    @Override
    protected void initialize(ModuleContext context, String name) {
        this.context = (TestModuleContext) context;
        this.name = name;
        config = this.context.getConfig(DBConfig.class, name);
    }

    @Override
    protected void validate() {
    }

    public void runScript(String scriptPath) {
        new SQLScriptRunner(config.database, ClasspathResources.text(scriptPath)).run();
    }

    public void createSchema() {
        List<Class<?>> entityClasses = config.entityClasses;
        for (Class<?> entityClass : entityClasses) {
            new EntitySchemaGenerator(config.database, entityClass).generate();
        }
    }

    public <T> Repository<T> repository(Class<T> entityClass) {
        return context.beanFactory.bean(Types.generic(Repository.class, entityClass), name);
    }
}
