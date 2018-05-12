package core.framework.module;

import core.framework.db.Repository;
import core.framework.impl.module.ModuleContext;
import core.framework.test.db.EntitySchemaGenerator;
import core.framework.test.db.SQLScriptRunner;
import core.framework.test.module.TestModuleContext;
import core.framework.util.ClasspathResources;
import core.framework.util.Exceptions;
import core.framework.util.Types;

import java.util.List;

/**
 * @author neo
 */
public final class InitDBConfig {
    private final DBConfig config;
    private final TestModuleContext context;
    private final String name;

    InitDBConfig(ModuleContext context, String name) {
        this.context = (TestModuleContext) context;
        this.name = name;
        config = this.context.findConfig(DBConfig.class, name)
                             .orElseThrow(() -> Exceptions.error("db({}) must be configured before initDB()", name == null ? "" : name));
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
