package core.framework.mongo.module;

import com.mongodb.ConnectionString;
import core.framework.impl.module.Config;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.module.ShutdownHook;
import core.framework.mongo.Mongo;
import core.framework.mongo.MongoCollection;
import core.framework.mongo.impl.MongoImpl;
import core.framework.util.Types;

import java.time.Duration;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class MongoConfig extends Config {
    protected MongoImpl mongo;
    String uri;
    private ModuleContext context;
    private String name;
    private boolean entityAdded;

    @Override
    protected void initialize(ModuleContext context, String name) {
        this.context = context;
        this.name = name;
        mongo = createMongo();
        context.beanFactory.bind(Mongo.class, name, mongo);
    }

    @Override
    protected void validate() {
        if (uri == null) throw new Error("mongo uri must be configured, name=" + name);
        if (!entityAdded)
            throw new Error("mongo is configured but no collection/view added, please remove unnecessary config, name=" + name);
    }

    private MongoImpl createMongo() {
        var mongo = new MongoImpl();
        context.startupHook.add(mongo::initialize);
        context.shutdownHook.add(ShutdownHook.STAGE_7, timeout -> mongo.close());
        return mongo;
    }

    public void uri(String uri) {
        if (this.uri != null) throw new Error(format("mongo uri is already configured, name={}, uri={}, previous={}", name, uri, this.uri));
        var connectionString = new ConnectionString(uri);
        if (connectionString.getDatabase() == null) throw new Error("uri must have database, uri=" + uri);
        mongo.uri = connectionString(connectionString);
        this.uri = uri;
    }

    ConnectionString connectionString(ConnectionString uri) {
        return uri;
    }

    public void poolSize(int minSize, int maxSize) {
        mongo.poolSize(minSize, maxSize);
    }

    public void slowOperationThreshold(Duration threshold) {
        mongo.slowOperationThreshold(threshold);
    }

    public void tooManyRowsReturnedThreshold(int threshold) {
        mongo.tooManyRowsReturnedThreshold = threshold;
    }

    public void timeout(Duration timeout) {
        mongo.timeout(timeout);
    }

    public <T> void collection(Class<T> entityClass) {
        if (uri == null) throw new Error("mongo uri must be configured first, name=" + name);
        context.beanFactory.bind(Types.generic(MongoCollection.class, entityClass), name, mongo.collection(entityClass));
        entityAdded = true;
    }

    public <T> void view(Class<T> viewClass) {
        if (uri == null) throw new Error("mongo uri must be configured first, name=" + name);
        mongo.view(viewClass);
        entityAdded = true;
    }
}
