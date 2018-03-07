package core.framework.test.module;

import core.framework.db.Repository;
import core.framework.impl.module.ModuleContext;
import core.framework.module.DBConfig;
import core.framework.test.db.EntitySchemaGenerator;
import core.framework.test.db.SQLScriptRunner;
import core.framework.util.ClasspathResources;
import core.framework.util.Types;

import java.util.List;

/**
 * @author neo
 */
public final class InitDBConfig {
    private final DBConfig.State state;
    private final ModuleContext context;
    private final String name;

    InitDBConfig(ModuleContext context, String name) {
        this.context = context;
        this.name = name;
        state = context.config.state("db:" + name);
    }

    public void runScript(String scriptPath) {
        new SQLScriptRunner(state.database, ClasspathResources.text(scriptPath)).run();
    }

    public void createSchema() {
        List<Class<?>> entityClasses = state.entityClasses;
        for (Class<?> entityClass : entityClasses) {
            new EntitySchemaGenerator(state.database, entityClass).generate();
        }
    }

    public <T> Repository<T> repository(Class<T> entityClass) {
        return context.beanFactory.bean(Types.generic(Repository.class, entityClass), name);
    }
}
