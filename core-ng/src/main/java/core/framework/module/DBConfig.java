package core.framework.module;

import core.framework.crypto.Password;
import core.framework.db.Database;
import core.framework.db.IsolationLevel;
import core.framework.db.Repository;
import core.framework.impl.db.DatabaseImpl;
import core.framework.impl.db.Vendor;
import core.framework.impl.module.Config;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.module.ShutdownHook;
import core.framework.impl.resource.PoolMetrics;
import core.framework.util.Lists;
import core.framework.util.Types;

import java.time.Duration;
import java.util.List;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class DBConfig extends Config {
    final List<Class<?>> entityClasses = Lists.newArrayList();
    protected String name;
    DatabaseImpl database;
    private ModuleContext context;
    private String url;
    private boolean entityAdded;

    @Override
    protected void initialize(ModuleContext context, String name) {
        this.context = context;
        this.name = name;
        this.database = createDatabase();
    }

    @Override
    protected void validate() {
        if (url == null) throw new Error(format("db url must be configured, name={}", name));
        if (!entityAdded)
            throw new Error(format("db is configured but no repository/view added, please remove unnecessary config, name={}", name));
    }

    private DatabaseImpl createDatabase() {
        var database = new DatabaseImpl("db" + (name == null ? "" : "-" + name));
        context.shutdownHook.add(ShutdownHook.STAGE_7, timeout -> database.close());
        context.backgroundTask().scheduleWithFixedDelay(database.pool::refresh, Duration.ofMinutes(10));
        context.stat.metrics.add(new PoolMetrics(database.pool));
        context.beanFactory.bind(Database.class, name, database);
        return database;
    }

    public void url(String url) {
        if (this.url != null) throw new Error(format("db url is already configured, name={}, url={}, previous={}", name, url, this.url));
        Vendor vendor = vendor(url);
        database.vendor = vendor;
        database.url(databaseURL(url, vendor));
        this.url = url;
    }

    String databaseURL(String url, Vendor vendor) {
        return url;
    }

    private Vendor vendor(String url) {
        if (url.startsWith("jdbc:mysql:")) {
            return Vendor.MYSQL;
        } else if (url.startsWith("jdbc:oracle:")) {
            return Vendor.ORACLE;
        }
        throw new Error(format("not supported database vendor, url={}", url));
    }

    public void user(String user) {
        database.user = user;
    }

    public void password(String password) {
        database.password = password;
    }

    public void encryptedPassword(String encryptedPassword, String privateKey) {
        String password = Password.decrypt(encryptedPassword, privateKey);
        password(password);
    }

    public void poolSize(int minSize, int maxSize) {
        database.pool.size(minSize, maxSize);
    }

    public void isolationLevel(IsolationLevel level) {
        database.isolationLevel = level;
    }

    public void slowOperationThreshold(Duration threshold) {
        database.slowOperationThresholdInNanos = threshold.toNanos();
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

    public void batchSize(int size) {
        database.operation.batchSize = size;
    }

    public void view(Class<?> viewClass) {
        if (url == null) throw new Error(format("db url must be configured first, name={}", name));
        database.view(viewClass);
        entityAdded = true;
    }

    public <T> void repository(Class<T> entityClass) {
        if (url == null) throw new Error(format("db url must be configured first, name={}", name));
        context.beanFactory.bind(Types.generic(Repository.class, entityClass), name, database.repository(entityClass));
        entityAdded = true;
        entityClasses.add(entityClass);
    }
}
