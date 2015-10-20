package core.framework.test.module;

import com.github.fakemongo.Fongo;
import core.framework.api.queue.MessagePublisher;
import core.framework.api.redis.Redis;
import core.framework.impl.module.MockFactory;
import core.framework.impl.queue.MessageValidator;
import core.framework.test.queue.MockMessagePublisher;
import core.framework.test.redis.MockRedis;
import org.mockito.Mockito;

/**
 * @author neo
 */
public final class MockFactoryImpl implements MockFactory {
    @Override
    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> instanceClass, Object... params) {
        if (Redis.class.equals(instanceClass)) return (T) new MockRedis();
        if (MessagePublisher.class.equals(instanceClass))
            return (T) new MockMessagePublisher<>((String) params[0], (MessageValidator) params[1]);
        // mongo jar is optional for project
        if ("com.mongodb.MongoClient".equals(instanceClass.getCanonicalName()))
            return (T) new Fongo("test").getMongo();
        return Mockito.mock(instanceClass);
    }
}
