package core.framework.module;

import core.framework.crypto.Password;
import core.framework.db.Database;
import core.framework.db.IsolationLevel;
import core.framework.db.Repository;
import core.framework.impl.db.DatabaseImpl;
import core.framework.impl.db.Vendor;
import core.framework.impl.module.Config;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.resource.PoolMetrics;
import core.framework.util.Exceptions;
import core.framework.util.Lists;
import core.framework.util.Strings;
import core.framework.util.Types;

import java.time.Duration;
import java.util.List;

/**
 * @author neo
 */
public final class DBConfig {
    final State state;
    private final ModuleContext context;
    private final String name;

    DBConfig(ModuleContext context, String name) {
        this.context = context;
        this.name = name;
        state = context.config.state("db:" + name, () -> new State(name));

        if (state.database == null) {
            state.database = createDatabase();
        }
    }

    private DatabaseImpl createDatabase() {
        DatabaseImpl database = new DatabaseImpl("db" + (name == null ? "" : "-" + name));
        context.shutdownHook.add(database::close);
        context.backgroundTask().scheduleWithFixedDelay(database.pool::refresh, Duration.ofMinutes(30));
        context.stat.metrics.add(new PoolMetrics(database.pool));
        context.beanFactory.bind(Database.class, name, database);
        return database;
    }

    public void url(String url) {
        if (state.url != null) throw Exceptions.error("db({}).url() is already configured, url={}, previous={}", name == null ? "" : name, url, state.url);
        state.database.vendor = vendor(url);
        if (!context.isTest()) {
            state.database.url(url);
        } else {
            String syntaxParam = hsqldbSyntaxParam();
            state.database.url(Strings.format("jdbc:hsqldb:mem:{};{}", name == null ? "." : name, syntaxParam));
        }
        state.url = url;
    }

    private String hsqldbSyntaxParam() {
        switch (state.database.vendor) {
            case ORACLE:
                return "sql.syntax_ora=true";
            case MYSQL:
                return "sql.syntax_mys=true";
            default:
                return "";
        }
    }

    private Vendor vendor(String url) {
        if (url.startsWith("jdbc:mysql:")) {
            return Vendor.MYSQL;
        } else if (url.startsWith("jdbc:oracle:")) {
            return Vendor.ORACLE;
        }
        throw Exceptions.error("not supported database vendor, url={}", url);
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

    public static class State implements Config.State {
        public final List<Class<?>> entityClasses = Lists.newArrayList();
        final String name;
        public DatabaseImpl database;
        String url;
        boolean entityAdded;

        public State(String name) {
            this.name = name;
        }

        @Override
        public void validate() {
            if (url == null) throw Exceptions.error("db({}).url() must be configured", name == null ? "" : name);
            if (!entityAdded)
                throw Exceptions.error("db({}) is configured but no repository/view added, please remove unnecessary config", name == null ? "" : name);
        }
    }
}
