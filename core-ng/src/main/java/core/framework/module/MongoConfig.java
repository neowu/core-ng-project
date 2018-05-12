package core.framework.module;

import core.framework.impl.module.ModuleContext;
import core.framework.impl.mongo.MongoImpl;
import core.framework.mongo.Mongo;
import core.framework.mongo.MongoCollection;
import core.framework.util.Exceptions;
import core.framework.util.Types;

import java.time.Duration;

/**
 * @author neo
 */
public class MongoConfig {
    private final ModuleContext context;
    private final String name;
    private final MongoImpl mongo;
    private String uri;
    private boolean entityAdded;

    MongoConfig(ModuleContext context, String name) {
        this.context = context;
        this.name = name;
        mongo = createMongo();
        context.beanFactory.bind(Mongo.class, name, mongo);
    }

    void validate() {
        if (uri == null) throw Exceptions.error("mongo({}).uri() must be configured", name == null ? "" : name);
        if (!entityAdded)
            throw Exceptions.error("mongo({}) is configured but no collection/view added, please remove unnecessary config", name == null ? "" : name);
    }

    MongoImpl createMongo() {
        MongoImpl mongo = new MongoImpl();
        context.startupHook.add(mongo::initialize);
        context.shutdownHook.add(mongo::close);
        return mongo;
    }

    public void uri(String uri) {
        if (this.uri != null) throw Exceptions.error("mongo({}).uri() is already configured, uri={}, previous={}", name == null ? "" : name, uri, this.uri);
        mongo.uri(uri);
        this.uri = uri;
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
        if (uri == null) throw Exceptions.error("mongo({}).uri() must be configured first", name == null ? "" : name);
        context.beanFactory.bind(Types.generic(MongoCollection.class, entityClass), name, mongo.collection(entityClass));
        entityAdded = true;
    }

    public <T> void view(Class<T> viewClass) {
        if (uri == null) throw Exceptions.error("mongo({}).uri() must be configured first", name == null ? "" : name);
        mongo.view(viewClass);
        entityAdded = true;
    }
}
