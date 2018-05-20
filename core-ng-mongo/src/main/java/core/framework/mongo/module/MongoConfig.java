package core.framework.mongo.module;

import core.framework.impl.module.Config;
import core.framework.impl.module.ModuleContext;
import core.framework.mongo.Mongo;
import core.framework.mongo.MongoCollection;
import core.framework.mongo.impl.MongoImpl;
import core.framework.util.Exceptions;
import core.framework.util.Types;

import java.time.Duration;

/**
 * @author neo
 */
public class MongoConfig extends Config {
    String uri;
    private ModuleContext context;
    private String name;
    private MongoImpl mongo;
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
        if (uri == null) throw Exceptions.error("mongo uri must be configured, name={}", name);
        if (!entityAdded)
            throw Exceptions.error("mongo is configured but no collection/view added, please remove unnecessary config, name={}", name);
    }

    MongoImpl createMongo() {
        MongoImpl mongo = new MongoImpl();
        context.startupHook.add(mongo::initialize);
        context.shutdownHook.add(mongo::close);
        return mongo;
    }

    public void uri(String uri) {
        if (this.uri != null) throw Exceptions.error("mongo uri is already configured, name={}, uri={}, previous={}", name, uri, this.uri);
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
        if (uri == null) throw Exceptions.error("mongo uri must be configured first, name={}", name);
        context.beanFactory.bind(Types.generic(MongoCollection.class, entityClass), name, mongo.collection(entityClass));
        entityAdded = true;
    }

    public <T> void view(Class<T> viewClass) {
        if (uri == null) throw Exceptions.error("mongo uri must be configured first, name={}", name);
        mongo.view(viewClass);
        entityAdded = true;
    }
}
