package core.framework.api.module;

import core.framework.api.mongo.Mongo;
import core.framework.api.mongo.MongoCollection;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Types;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.mongo.MongoImpl;

import java.time.Duration;

/**
 * @author neo
 */
public final class MongoConfig {
    private final ModuleContext context;
    private final MongoImpl mongo;
    private final String name;
    private final MongoConfigState state;

    public MongoConfig(ModuleContext context, String name) {
        this.context = context;
        this.name = name;

        if (context.beanFactory.registered(Mongo.class, name)) {
            mongo = context.beanFactory.bean(Mongo.class, name);
        } else {
            if (context.isTest()) {
                mongo = context.mockFactory.create(MongoImpl.class);
            } else {
                mongo = new MongoImpl();
                context.startupHook.add(mongo::initialize);
                context.shutdownHook.add(mongo::close);
            }
            context.beanFactory.bind(Mongo.class, name, mongo);
        }

        state = context.config.mongo(name);
    }

    public void uri(String uri) {
        mongo.uri(uri);
        state.uri = uri;
    }

    public void poolSize(int minSize, int maxSize) {
        mongo.poolSize(minSize, maxSize);
    }

    public void slowOperationThreshold(Duration threshold) {
        mongo.slowOperationThreshold(threshold);
    }

    public void tooManyRowsReturnedThreshold(int tooManyRowsReturnedThreshold) {
        mongo.tooManyRowsReturnedThreshold(tooManyRowsReturnedThreshold);
    }

    public void timeout(Duration timeout) {
        mongo.timeout(timeout);
    }

    public <T> void collection(Class<T> entityClass) {
        if (state.uri == null) throw Exceptions.error("mongo({}).uri() must be configured first", name == null ? "" : name);
        context.beanFactory.bind(Types.generic(MongoCollection.class, entityClass), name, mongo.collection(entityClass));
        state.entityAdded = true;
    }

    public <T> void view(Class<T> viewClass) {
        if (state.uri == null) throw Exceptions.error("mongo({}).uri() must be configured first", name == null ? "" : name);
        mongo.view(viewClass);
        state.entityAdded = true;
    }

    public static class MongoConfigState {
        final String name;
        String uri;
        boolean entityAdded;

        public MongoConfigState(String name) {
            this.name = name;
        }

        public void validate() {
            if (uri == null) throw Exceptions.error("mongo({}).uri() must be configured", name == null ? "" : name);
            if (!entityAdded)
                throw Exceptions.error("mongo({}) is configured but no collection/view added, please remove unnecessary config", name == null ? "" : name);
        }
    }
}
