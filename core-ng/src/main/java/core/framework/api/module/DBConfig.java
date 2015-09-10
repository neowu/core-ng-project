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
    private final Logger logger = LoggerFactory.getLogger(DBConfig.class);
    private final ModuleContext context;
    private final String name;
    final DatabaseImpl database;

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

    public DBConfig url(String url) {
        if (context.test) {
            logger.info("use hsqldb during test");
            database.url(Strings.format("jdbc:hsqldb:mem:{};sql.syntax_mys=true", name == null ? "." : name));
        } else {
            database.url(url);
        }
        return this;
    }

    public DBConfig user(String user) {
        if (!context.test) {
            database.user(user);
        }
        return this;
    }

    public DBConfig password(String password) {
        if (!context.test) {
            database.password(password);
        }
        return this;
    }

    public DBConfig encryptedPassword(String encryptedPassword, String privateKey) {
        String password = Password.decrypt(encryptedPassword, privateKey);
        return password(password);
    }

    public DBConfig poolSize(int minSize, int maxSize) {
        database.pool.size(minSize, maxSize);
        return this;
    }

    public DBConfig defaultIsolationLevel(IsolationLevel defaultIsolationLevel) {
        database.operation.transactionManager.defaultIsolationLevel = defaultIsolationLevel;
        return this;
    }

    public DBConfig slowQueryThreshold(Duration slowQueryThreshold) {
        database.slowQueryThresholdInMs = slowQueryThreshold.toMillis();
        return this;
    }

    public DBConfig tooManyRowsReturnedThreshold(int tooManyRowsReturnedThreshold) {
        database.tooManyRowsReturnedThreshold = tooManyRowsReturnedThreshold;
        return this;
    }

    public DBConfig longTransactionThreshold(Duration longTransactionThreshold) {
        database.operation.transactionManager.longTransactionThresholdInMs = longTransactionThreshold.toMillis();
        return this;
    }

    public DBConfig timeout(Duration timeout) {
        database.timeout(timeout);
        return this;
    }

    public <T> DBConfig view(Class<T> viewClass) {
        database.view(viewClass);
        return this;
    }

    public <T> void repository(Class<T> entityClass) {
        context.beanFactory.bind(Types.generic(Repository.class, entityClass), name, database.repository(entityClass));
    }
}
