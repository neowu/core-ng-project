package core.framework.module;

import core.framework.impl.module.Config;
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
public final class MongoConfig {
    private final ModuleContext context;
    private final String name;
    private final State state;

    MongoConfig(ModuleContext context, String name) {
        this.context = context;
        this.name = name;
        state = context.config.state("mongo:" + name, () -> new State(name));

        if (state.mongo == null) {
            state.mongo = createMongo();
        }
    }

    private MongoImpl createMongo() {
        MongoImpl mongo;
        if (context.isTest()) {
            mongo = context.mockFactory.create(MongoImpl.class);
        } else {
            mongo = new MongoImpl();
            context.startupHook.add(mongo::initialize);
            context.shutdownHook.add(mongo::close);
        }
        context.beanFactory.bind(Mongo.class, name, mongo);
        return mongo;
    }

    public void uri(String uri) {
        if (state.uri != null) throw Exceptions.error("mongo({}).uri() is already configured, uri={}, previous={}", name == null ? "" : name, uri, state.uri);
        state.mongo.uri(uri);
        state.uri = uri;
    }

    public void poolSize(int minSize, int maxSize) {
        state.mongo.poolSize(minSize, maxSize);
    }

    public void slowOperationThreshold(Duration threshold) {
        state.mongo.slowOperationThreshold(threshold);
    }

    public void tooManyRowsReturnedThreshold(int threshold) {
        state.mongo.tooManyRowsReturnedThreshold = threshold;
    }

    public void timeout(Duration timeout) {
        state.mongo.timeout(timeout);
    }

    public <T> void collection(Class<T> entityClass) {
        if (state.uri == null) throw Exceptions.error("mongo({}).uri() must be configured first", name == null ? "" : name);
        context.beanFactory.bind(Types.generic(MongoCollection.class, entityClass), name, state.mongo.collection(entityClass));
        state.entityAdded = true;
    }

    public <T> void view(Class<T> viewClass) {
        if (state.uri == null) throw Exceptions.error("mongo({}).uri() must be configured first", name == null ? "" : name);
        state.mongo.view(viewClass);
        state.entityAdded = true;
    }

    public static class State implements Config.State {
        final String name;
        MongoImpl mongo;
        String uri;
        boolean entityAdded;

        public State(String name) {
            this.name = name;
        }

        @Override
        public void validate() {
            if (uri == null) throw Exceptions.error("mongo({}).uri() must be configured", name == null ? "" : name);
            if (!entityAdded)
                throw Exceptions.error("mongo({}) is configured but no collection/view added, please remove unnecessary config", name == null ? "" : name);
        }
    }
}
