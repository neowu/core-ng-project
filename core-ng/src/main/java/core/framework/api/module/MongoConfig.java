package core.framework.api.module;

import com.mongodb.MongoClient;
import core.framework.api.mongo.Mongo;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.mongo.MongoImpl;

import java.time.Duration;

/**
 * @author neo
 */
public class MongoConfig {
    private final ModuleContext context;
    private final MongoImpl mongo;

    public MongoConfig(ModuleContext context) {
        this.context = context;
        if (context.beanFactory.registered(Mongo.class, null)) {
            mongo = context.beanFactory.bean(Mongo.class, null);
        } else {
            mongo = new MongoImpl();
            if (context.isTest()) {
                mongo.mongoClient = context.mockFactory.create(MongoClient.class);
            } else {
                context.shutdownHook.add(mongo::close);
            }
            context.beanFactory.bind(Mongo.class, null, mongo);
        }
    }

    public void uri(String uri) {
        if (context.isTest()) {
            mongo.uri("mongodb://localhost/test");
        } else {
            mongo.uri(uri);
        }
    }

    public void poolSize(int minSize, int maxSize) {
        mongo.poolSize(minSize, maxSize);
    }

    public void slowQueryThreshold(Duration slowQueryThreshold) {
        mongo.slowQueryThreshold(slowQueryThreshold);
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
