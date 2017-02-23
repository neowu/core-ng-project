package core.framework.api.module;

import core.framework.api.crypto.Password;
import core.framework.api.db.Database;
import core.framework.api.db.IsolationLevel;
import core.framework.api.db.Repository;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Lists;
import core.framework.api.util.Strings;
import core.framework.api.util.Types;
import core.framework.impl.db.DatabaseImpl;
import core.framework.impl.module.ModuleContext;

import java.time.Duration;
import java.util.List;

/**
 * @author neo
 */
public final class DBConfig {
    final DatabaseImpl database;
    private final ModuleContext context;
    private final String name;
    private final DBConfigState state;

    public DBConfig(ModuleContext context, String name) {
        this.context = context;
        this.name = name;
        if (context.beanFactory.registered(Database.class, name)) {
            database = context.beanFactory.bean(Database.class, name);
        } else {
            database = new DatabaseImpl();
            database.pool.name("db" + (name == null ? "" : "-" + name));
            context.shutdownHook.add(database::close);
            if (!context.isTest()) {
                context.backgroundTask().scheduleWithFixedDelay(database.pool::refresh, Duration.ofMinutes(30));
            }
            context.beanFactory.bind(Database.class, name, database);
        }
        state = context.config.db(name);
    }

    public void url(String url) {
        if (state.url != null) throw Exceptions.error("db({}).url() is already configured, url={}, previous={}", name == null ? "" : name, url, state.url);
        if (!context.isTest()) {
            database.url(url);
        } else {
            database.url(Strings.format("jdbc:hsqldb:mem:{};sql.syntax_mys=true", name == null ? "." : name));
        }
        state.url = url;
    }

    public void user(String user) {
        if (!context.isTest()) {
            database.user(user);
        }
    }

    public void password(String password) {
        if (!context.isTest()) {
            database.password(password);
        }
    }

    public void encryptedPassword(String encryptedPassword, String privateKey) {
        String password = Password.decrypt(encryptedPassword, privateKey);
        password(password);
    }

    public void poolSize(int minSize, int maxSize) {
        database.pool.size(minSize, maxSize);
    }

    public void defaultIsolationLevel(IsolationLevel level) {
        database.operation.transactionManager.defaultIsolationLevel = level;
    }

    public void slowOperationThreshold(Duration threshold) {
        database.slowOperationThreshold(threshold);
    }

    public void tooManyRowsReturnedThreshold(int threshold) {
        database.tooManyRowsReturnedThreshold = threshold;
    }

    public void longTransactionThreshold(Duration threshold) {
        database.operation.transactionManager.longTransactionThresholdInNanos = threshold.toNanos();
    }

    public void timeout(Duration timeout) {
        database.timeout(timeout);
    }

    public void view(Class<?> viewClass) {
        if (state.url == null) throw Exceptions.error("db({}).url() must be configured first", name == null ? "" : name);
        database.view(viewClass);
        state.entityAdded = true;
    }

    public <T> void repository(Class<T> entityClass) {
        if (state.url == null) throw Exceptions.error("db({}).url() must be configured first", name == null ? "" : name);
        context.beanFactory.bind(Types.generic(Repository.class, entityClass), name, database.repository(entityClass));
        state.entityAdded = true;
        state.entityClasses.add(entityClass);
    }

    public static class DBConfigState {
        public final List<Class<?>> entityClasses = Lists.newArrayList();
        final String name;
        String url;
        boolean entityAdded;

        public DBConfigState(String name) {
            this.name = name;
        }

        public void validate() {
            if (url == null) throw Exceptions.error("db({}).url() must be configured", name == null ? "" : name);
            if (!entityAdded)
                throw Exceptions.error("db({}) is configured but no repository/view added, please remove unnecessary config", name == null ? "" : name);
        }
    }
}
