package core.framework.test.module;

import core.framework.db.Repository;
import core.framework.module.DBConfig;
import core.framework.test.db.EntitySchemaGenerator;
import core.framework.test.db.SQLScriptRunner;
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

    InitDBConfig(TestModuleContext context, String name) {
        this.context = context;
        this.name = name;
        config = context.findConfig(DBConfig.class, name)
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
