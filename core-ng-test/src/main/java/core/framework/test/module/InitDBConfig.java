package core.framework.test.module;

import core.framework.db.Database;
import core.framework.db.Repository;
import core.framework.impl.module.ModuleContext;
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
    private final ModuleContext context;
    private final String name;
    private final Database database;

    public InitDBConfig(ModuleContext context, String name) {
        this.context = context;
        this.name = name;
        if (!context.beanFactory.registered(Database.class, name)) {
            throw Exceptions.error("db({}) is not configured, name={}", name == null ? "" : name);
        }
        database = context.beanFactory.bean(Database.class, name);
    }

    public void runScript(String scriptPath) {
        new SQLScriptRunner(database, ClasspathResources.text(scriptPath)).run();
    }

    public void createSchema() {
        List<Class<?>> entityClasses = context.config.db(name).entityClasses;
        for (Class<?> entityClass : entityClasses) {
            new EntitySchemaGenerator(database, entityClass).generate();
        }
    }

    public <T> Repository<T> repository(Class<T> entityClass) {
        return context.beanFactory.bean(Types.generic(Repository.class, entityClass), name);
    }
}
