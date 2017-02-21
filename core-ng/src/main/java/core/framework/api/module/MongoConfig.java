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
            context.validators.add(() -> {
                if (mongo.uri == null) throw Exceptions.error("mongo({}).uri() must be configured", name == null ? "" : name);
            });
        }
    }

    public void uri(String uri) {
        mongo.uri(uri);
    }

    public void poolSize(int minSize, int maxSize) {
        if (!context.isTest()) {
            mongo.poolSize(minSize, maxSize);
        }
    }

    public void slowOperationThreshold(Duration threshold) {
        if (!context.isTest()) {
            mongo.slowOperationThreshold(threshold);
        }
    }

    public void tooManyRowsReturnedThreshold(int tooManyRowsReturnedThreshold) {
        if (!context.isTest()) {
            mongo.tooManyRowsReturnedThreshold(tooManyRowsReturnedThreshold);
        }
    }

    public void timeout(Duration timeout) {
        if (!context.isTest()) {
            mongo.timeout(timeout);
        }
    }

    public <T> void collection(Class<T> entityClass) {
        context.beanFactory.bind(Types.generic(MongoCollection.class, entityClass), name, mongo.collection(entityClass));
    }

    public <T> void view(Class<T> viewClass) {
        mongo.view(viewClass);
    }
}
