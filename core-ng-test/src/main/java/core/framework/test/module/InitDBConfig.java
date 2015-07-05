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
    private final ModuleContext moduleContext;
    private final String name;
    private final Database database;

    public InitDBConfig(ModuleContext moduleContext, String name) {
        this.moduleContext = moduleContext;
        this.name = name;

        if (!moduleContext.beanFactory.registered(Database.class, name)) {
            throw Exceptions.error("database is not configured, please use db() to configure, name={}", name);
        }

        database = moduleContext.beanFactory.bean(Database.class, name);
    }

    public InitDBConfig runScript(String scriptPath) {
        new SQLScriptRunner(database, ClasspathResources.text(scriptPath)).run();
        return this;
    }

    public InitDBConfig createSchema() {
        Set<Key> keys = moduleContext.beanFactory.keys();
        for (Key key : keys) {
            Type type = key.type;
            if (!(type instanceof ParameterizedType)) continue;
            Class<?> beanClass = (Class) ((ParameterizedType) type).getRawType();
            if (Objects.equals(beanClass, Repository.class) && Strings.equals(key.name, name)) {
                Class<?> entityClass = (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
                new EntitySchemaGenerator(database, entityClass).generate();
            }
        }
        return this;
    }

    public <T> Repository<T> repository(Class<T> entityClass) {
        return moduleContext.beanFactory.bean(Types.generic(Repository.class, entityClass), name);
    }
}
