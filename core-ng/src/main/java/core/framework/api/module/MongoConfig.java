package core.framework.api.module;

import core.framework.api.mongo.Mongo;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.mongo.MongoImpl;
import core.framework.impl.mongo.MongoOption;

import java.time.Duration;

/**
 * @author neo
 */
public final class MongoConfig {
    private final MongoOption mongo;

    public MongoConfig(ModuleContext context) {
        if (context.beanFactory.registered(Mongo.class, null)) {
            mongo = context.beanFactory.bean(Mongo.class, null);
        } else {
            if (context.isTest()) {
                mongo = (MongoOption) context.mockFactory.create(Mongo.class);
            } else {
                MongoImpl mongo = new MongoImpl();
                context.startupHook.add(mongo::initialize);
                context.shutdownHook.add(mongo::close);
                this.mongo = mongo;
            }
            context.beanFactory.bind(Mongo.class, null, mongo);
        }
    }

    public void uri(String uri) {
        mongo.uri(uri);
    }

    public void poolSize(int minSize, int maxSize) {
        mongo.poolSize(minSize, maxSize);
    }

    public void slowOperationThreshold(Duration threshold) {
        mongo.slowOperationThreshold(threshold);
    }

    public void tooManyRowsReturnedThreshold(int tooManyRowsReturnedThreshold) {
        mongo.setTooManyRowsReturnedThreshold(tooManyRowsReturnedThreshold);
    }

    public void timeout(Duration timeout) {
        mongo.timeout(timeout);
    }

    public <T> void entityClass(Class<T> entityClass) {
        mongo.entityClass(entityClass);
    }

    public <T> void viewClass(Class<T> viewClass) {
        mongo.viewClass(viewClass);
    }
}
