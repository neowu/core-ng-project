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
    final State state;
    private final ModuleContext context;
    private final String name;

    public DBConfig(ModuleContext context, String name) {
        this.context = context;
        this.name = name;
        state = context.config.db(name);

        if (state.database == null) {
            state.database = createDatabase();
        }
    }

    private DatabaseImpl createDatabase() {
        DatabaseImpl database = new DatabaseImpl();
        database.pool.name("db" + (name == null ? "" : "-" + name));
        context.shutdownHook.add(database::close);
        if (!context.isTest()) {
            context.backgroundTask().scheduleWithFixedDelay(database.pool::refresh, Duration.ofMinutes(30));
        }
        context.beanFactory.bind(Database.class, name, database);
        return database;
    }

    public void url(String url) {
        if (state.url != null) throw Exceptions.error("db({}).url() is already configured, url={}, previous={}", name == null ? "" : name, url, state.url);
        if (!context.isTest()) {
            state.database.url(url);
        } else {
            String syntaxParam = "";
            if (url.startsWith("jdbc:oracle:")) {
                syntaxParam = "sql.syntax_ora=true";
            } else if (url.startsWith("jdbc:mysql:")) {
                syntaxParam = "sql.syntax_mys=true";
            }
            state.database.url(Strings.format("jdbc:hsqldb:mem:{};{}", name == null ? "." : name, syntaxParam));
        }
        state.url = url;
    }

    public void user(String user) {
        if (!context.isTest()) {
            state.database.user = user;
        }
    }

    public void password(String password) {
        if (!context.isTest()) {
            state.database.password = password;
        }
    }

    public void encryptedPassword(String encryptedPassword, String privateKey) {
        String password = Password.decrypt(encryptedPassword, privateKey);
        password(password);
    }

    public void poolSize(int minSize, int maxSize) {
        state.database.pool.size(minSize, maxSize);
    }

    public void defaultIsolationLevel(IsolationLevel level) {
        state.database.operation.transactionManager.defaultIsolationLevel = level;
    }

    public void slowOperationThreshold(Duration threshold) {
        state.database.slowOperationThreshold(threshold);
    }

    public void tooManyRowsReturnedThreshold(int threshold) {
        state.database.tooManyRowsReturnedThreshold = threshold;
    }

    public void longTransactionThreshold(Duration threshold) {
        state.database.operation.transactionManager.longTransactionThresholdInNanos = threshold.toNanos();
    }

    public void timeout(Duration timeout) {
        state.database.timeout(timeout);
    }

    public void view(Class<?> viewClass) {
        if (state.url == null) throw Exceptions.error("db({}).url() must be configured first", name == null ? "" : name);
        state.database.view(viewClass);
        state.entityAdded = true;
    }

    public <T> void repository(Class<T> entityClass) {
        if (state.url == null) throw Exceptions.error("db({}).url() must be configured first", name == null ? "" : name);
        context.beanFactory.bind(Types.generic(Repository.class, entityClass), name, state.database.repository(entityClass));
        state.entityAdded = true;
        state.entityClasses.add(entityClass);
    }

    public static class State {
        public final List<Class<?>> entityClasses = Lists.newArrayList();
        final String name;
        DatabaseImpl database;
        String url;
        boolean entityAdded;

        public State(String name) {
            this.name = name;
        }

        public void validate() {
            if (url == null) throw Exceptions.error("db({}).url() must be configured", name == null ? "" : name);
            if (!entityAdded)
                throw Exceptions.error("db({}) is configured but no repository/view added, please remove unnecessary config", name == null ? "" : name);
        }
    }
}
