package core.framework.mongo.module;

import com.mongodb.ConnectionString;
import core.framework.internal.module.Config;
import core.framework.internal.module.ModuleContext;
import core.framework.internal.module.ReadinessProbe;
import core.framework.internal.module.ShutdownHook;
import core.framework.mongo.Mongo;
import core.framework.mongo.MongoCollection;
import core.framework.mongo.impl.MongoConnectionPoolMetrics;
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

        var mongo = new MongoImpl();
        mongo.metrics = new MongoConnectionPoolMetrics(name);
        this.context.startupHook.initialize.add(mongo::initialize);
        this.context.shutdownHook.add(ShutdownHook.STAGE_6, timeout -> mongo.close());
        context.collector.metrics.add(mongo.metrics);
        context.beanFactory.bind(Mongo.class, name, mongo);
        this.mongo = mongo;
    }

    @Override
    protected void validate() {
        if (uri == null) throw new Error("mongo uri must be configured, name=" + name);
        if (!entityAdded)
            throw new Error("mongo is configured but no collection/view added, please remove unnecessary config, name=" + name);
    }

    public void uri(String uri) {
        if (this.uri != null) throw new Error(format("mongo uri is already configured, name={}, uri={}, previous={}", name, uri, this.uri));
        var connectionString = new ConnectionString(uri);
        if (connectionString.getDatabase() == null) throw new Error("uri must have database, uri=" + uri);
        mongo.uri = connectionString(connectionString);
        addProbe(context.probe, connectionString);
        this.uri = uri;
    }

    void addProbe(ReadinessProbe probe, ConnectionString connectionString) {
        if (!connectionString.isSrvProtocol()) {
            // mongodb+srv protocol is generally used by Mongo Atlas, as DNS seed list
            // readiness probe is mainly used by self-hosting mongo within kube, no need to check dns with srv
            probe.hostURIs.add(connectionString.getHosts().get(0));
        }
    }

    ConnectionString connectionString(ConnectionString uri) {
        return uri;
    }

    public void poolSize(int minSize, int maxSize) {
        mongo.poolSize(minSize, maxSize);
    }

    public void timeout(Duration timeout) {
        mongo.timeout(timeout);
    }

    public <T> MongoCollection<T> collection(Class<T> entityClass) {
        if (uri == null) throw new Error("mongo uri must be configured first, name=" + name);
        MongoCollection<T> collection = mongo.collection(entityClass);
        context.beanFactory.bind(Types.generic(MongoCollection.class, entityClass), name, collection);
        entityAdded = true;
        return collection;
    }

    public <T> void view(Class<T> viewClass) {
        if (uri == null) throw new Error("mongo uri must be configured first, name=" + name);
        mongo.view(viewClass);
        entityAdded = true;
    }
}
