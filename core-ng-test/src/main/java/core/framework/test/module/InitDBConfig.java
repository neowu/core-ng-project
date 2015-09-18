package core.framework.test.module;

import core.framework.api.db.Database;
import core.framework.api.db.Repository;
import core.framework.api.util.ClasspathResources;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Strings;
import core.framework.api.util.Types;
import core.framework.impl.inject.Key;
import core.framework.impl.module.ModuleContext;
import core.framework.test.db.EntitySchemaGenerator;
import core.framework.test.db.SQLScriptRunner;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Set;

/**
 * @author neo
 */
public class InitDBConfig {
    private final ModuleContext context;
    private final String name;
    private final Database database;

    public InitDBConfig(ModuleContext context, String name) {
        this.context = context;
        this.name = name;

        if (!context.beanFactory.registered(Database.class, name)) {
            throw Exceptions.error("database is not configured, please use db() to configure, name={}", name);
        }

        database = context.beanFactory.bean(Database.class, name);
    }

    public void runScript(String scriptPath) {
        new SQLScriptRunner(database, ClasspathResources.text(scriptPath)).run();
    }

    public void createSchema() {
        Set<Key> keys = context.beanFactory.keys();
        for (Key key : keys) {
            Type type = key.type;
            if (!(type instanceof ParameterizedType)) continue;
            Class<?> beanClass = (Class) ((ParameterizedType) type).getRawType();
            if (Objects.equals(beanClass, Repository.class) && Strings.equals(key.name, name)) {
                Class<?> entityClass = (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
                new EntitySchemaGenerator(database, entityClass).generate();
            }
        }
    }

    public <T> Repository<T> repository(Class<T> entityClass) {
        return context.beanFactory.bean(Types.generic(Repository.class, entityClass), name);
    }
}
