package core.framework.test.module;

import core.framework.api.mongo.Mongo;
import core.framework.api.redis.Redis;
import core.framework.impl.module.MockFactory;
import core.framework.impl.queue.RabbitMQ;
import core.framework.impl.search.ElasticSearch;
import core.framework.test.mongo.MockMongo;
import core.framework.test.queue.MockRabbitMQ;
import core.framework.test.redis.MockRedis;
import core.framework.test.search.MockElasticSearch;
import org.mockito.Mockito;

import java.nio.file.Path;

/**
 * @author neo
 */
public final class MockFactoryImpl implements MockFactory {
    @Override
    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> instanceClass, Object... params) {
        if (Redis.class.equals(instanceClass)) return (T) new MockRedis();
        if (RabbitMQ.class.equals(instanceClass)) return (T) new MockRabbitMQ();
        if (Mongo.class.equals(instanceClass)) return (T) new MockMongo();
        if (ElasticSearch.class.equals(instanceClass)) return (T) new MockElasticSearch((Path) params[0]);
        return Mockito.mock(instanceClass);
    }
}
