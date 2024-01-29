package core.framework.module;

import core.framework.db.CloudAuthProvider;
import core.framework.db.Database;
import core.framework.db.IsolationLevel;
import core.framework.db.Repository;
import core.framework.internal.db.DatabaseImpl;
import core.framework.internal.db.cloud.AzureAuthProvider;
import core.framework.internal.db.cloud.GCloudAuthProvider;
import core.framework.internal.module.Config;
import core.framework.internal.module.ModuleContext;
import core.framework.internal.module.ShutdownHook;
import core.framework.internal.resource.PoolMetrics;
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

        var database = new DatabaseImpl("db" + (name == null ? "" : "-" + name));
        context.shutdownHook.add(ShutdownHook.STAGE_6, timeout -> database.close());
        context.backgroundTask().scheduleWithFixedDelay(database.pool::refresh, Duration.ofMinutes(10));
        context.collector.metrics.add(new PoolMetrics(database.pool));
        context.beanFactory.bind(Database.class, name, database);
        this.database = database;
    }

    @Override
    protected void validate() {
        if (url == null) throw new Error("db url must be configured, name=" + name);
        if (!entityAdded)
            throw new Error("db is configured but no repository/view added, please remove unnecessary config, name=" + name);
    }

    public void url(String url) {
        if (this.url != null) throw new Error(format("db url is already configured, name={}, url={}, previous={}", name, url, this.url));
        database.url(databaseURL(url));
        this.url = url;
    }

    String databaseURL(String url) {
        return url;
    }

    public void user(String user) {
        if (user.startsWith("iam/")) {
            CloudAuthProvider provider = CloudAuthProvider.Provider.get();
            if (provider == null) {
                provider = provider(user);
                CloudAuthProvider.Provider.set(provider);
            }
            database.authProvider = provider;
            context.logManager.maskFields("access_token");  // mask token from IAM http response, gcloud/azure all use JWT token
        } else {
            database.user = user;
        }
    }

    private CloudAuthProvider provider(String user) {
        return switch (user) {
            case "iam/gcloud" -> new GCloudAuthProvider();
            case "iam/azure" -> new AzureAuthProvider();
            case null, default -> throw new Error("unsupported cloud provider, value=" + user);
        };
    }

    public void password(String password) {
        database.password = password;
    }

    public void poolSize(int minSize, int maxSize) {
        database.pool.size(minSize, maxSize);
    }

    public void isolationLevel(IsolationLevel level) {
        database.isolationLevel = level;
    }

    public void longTransactionThreshold(Duration threshold) {
        database.operation.transactionManager.longTransactionThresholdInNanos = threshold.toNanos();
    }

    public void timeout(Duration timeout) {
        database.timeout(timeout);
    }

    public void view(Class<?> viewClass) {
        if (url == null) throw new Error("db url must be configured first, name=" + name);
        database.view(viewClass);
        entityAdded = true;
    }

    public <T> Repository<T> repository(Class<T> entityClass) {
        if (url == null) throw new Error("db url must be configured first, name=" + name);
        Repository<T> repository = database.repository(entityClass);
        context.beanFactory.bind(Types.generic(Repository.class, entityClass), name, repository);
        entityAdded = true;
        entityClasses.add(entityClass);
        return repository;
    }
}
