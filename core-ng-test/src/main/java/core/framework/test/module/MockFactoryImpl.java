package core.framework.test.module;

import core.framework.async.Executor;
import core.framework.impl.kafka.Kafka;
import core.framework.impl.module.MockFactory;
import core.framework.impl.mongo.MongoImpl;
import core.framework.impl.search.ElasticSearchImpl;
import core.framework.redis.Redis;
import core.framework.test.async.MockExecutor;
import core.framework.test.kafka.MockKafka;
import core.framework.test.mongo.MockMongo;
import core.framework.test.redis.MockRedis;
import core.framework.test.search.ESLoggerConfigFactory;
import core.framework.test.search.MockElasticSearch;
import org.mockito.Mockito;

import java.nio.file.Path;

/**
 * @author neo
 */
public final class MockFactoryImpl implements MockFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> T create(Class<T> instanceClass, Object... params) {
        if (Redis.class.equals(instanceClass)) return (T) new MockRedis();
        if (MongoImpl.class.equals(instanceClass)) return (T) new MockMongo();
        if (Kafka.class.equals(instanceClass)) return (T) new MockKafka();
        if (ElasticSearchImpl.class.equals(instanceClass)) {
            bindESLogger();
            return (T) new MockElasticSearch((Path) params[0]);
        }
        if (Executor.class.equals(instanceClass)) return (T) new MockExecutor();
        return Mockito.mock(instanceClass);
    }

    // es refers to log4j core directly in org.elasticsearch.common.logging.Loggers, this is to bridge es log to coreng logger
    // log4j-to-slf4j works if only transport client is used, but our integration test uses Node.
    // refer to org.elasticsearch.index.IndexModule(), in org.elasticsearch.index.SearchSlowLog(), setLevel calls log4j.core api
    private void bindESLogger() {
        if (System.getProperty("log4j.configurationFactory") != null) return;
        System.setProperty("log4j.configurationFactory", ESLoggerConfigFactory.class.getName());
        System.setProperty("log4j2.disable.jmx", "true");
        ESLoggerConfigFactory.bindLogger();
    }
}
