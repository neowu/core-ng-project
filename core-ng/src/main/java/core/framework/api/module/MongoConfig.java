package core.framework.api.module;

import core.framework.api.mongo.MongoCollection;
import core.framework.api.util.Types;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.mongo.Mongo;

import java.time.Duration;

/**
 * @author neo
 */
public final class MongoConfig {
    private final ModuleContext context;
    private final Mongo mongo;

    public MongoConfig(ModuleContext context) {
        this.context = context;
        if (context.beanFactory.registered(Mongo.class, null)) {
            mongo = context.beanFactory.bean(Mongo.class, null);
        } else {
            if (context.isTest()) {
                mongo = context.mockFactory.create(Mongo.class);
            } else {
                mongo = new Mongo();
                context.startupHook.add(mongo::initialize);
                context.shutdownHook.add(mongo::close);
            }
            context.beanFactory.bind(Mongo.class, null, mongo);
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
        context.beanFactory.bind(Types.generic(MongoCollection.class, entityClass), null, mongo.collection(entityClass));
    }

    public <T> void view(Class<T> viewClass) {
        mongo.view(viewClass);
    }
}
