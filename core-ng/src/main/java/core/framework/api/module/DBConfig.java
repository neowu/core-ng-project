package core.framework.api.module;

import core.framework.api.crypto.Password;
import core.framework.api.db.Database;
import core.framework.api.db.IsolationLevel;
import core.framework.api.db.Repository;
import core.framework.api.util.Strings;
import core.framework.api.util.Types;
import core.framework.impl.db.DatabaseImpl;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.resource.RefreshPoolJob;
import core.framework.impl.scheduler.FixedRateTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * @author neo
 */
public final class DBConfig {
    final DatabaseImpl database;
    private final Logger logger = LoggerFactory.getLogger(DBConfig.class);
    private final ModuleContext context;
    private final String name;

    public DBConfig(ModuleContext context, String name) {
        this.context = context;
        this.name = name;

        if (context.beanFactory.registered(Database.class, name)) {
            database = context.beanFactory.bean(Database.class, name);
        } else {
            database = new DatabaseImpl();
            context.shutdownHook.add(database::close);
            String poolName = "db" + (name == null ? "" : "-" + name);
            database.pool.name(poolName);
            context.scheduler().addTrigger(new FixedRateTrigger("refresh-" + poolName + "-pool", new RefreshPoolJob(database.pool), Duration.ofMinutes(15)));
            context.beanFactory.bind(Database.class, name, database);
        }
    }

    public void url(String url) {
        if (context.isTest()) {
            logger.info("use hsqldb during test");
            database.url(Strings.format("jdbc:hsqldb:mem:{};sql.syntax_mys=true", name == null ? "." : name));
        } else {
            database.url(url);
        }
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

    public void defaultIsolationLevel(IsolationLevel defaultIsolationLevel) {
        database.operation.transactionManager.defaultIsolationLevel = defaultIsolationLevel;
    }

    public void slowQueryThreshold(Duration slowQueryThreshold) {
        database.slowQueryThreshold(slowQueryThreshold);
    }

    public void tooManyRowsReturnedThreshold(int tooManyRowsReturnedThreshold) {
        database.tooManyRowsReturnedThreshold = tooManyRowsReturnedThreshold;
    }

    public void longTransactionThreshold(Duration longTransactionThreshold) {
        database.operation.transactionManager.longTransactionThresholdInMs = longTransactionThreshold.toMillis();
    }

    public void timeout(Duration timeout) {
        database.timeout(timeout);
    }

    public void view(Class<?> viewClass) {
        database.view(viewClass);
    }

    public <T> void repository(Class<T> entityClass) {
        context.beanFactory.bind(Types.generic(Repository.class, entityClass), name, database.repository(entityClass));
    }
}
