package core.framework.impl.mongo;

import java.time.Duration;

/**
 * @author neo
 */
public interface MongoOption {
    void uri(String uri);

    void poolSize(int minSize, int maxSize);

    void timeout(Duration timeout);

    <T> void entityClass(Class<T> entityClass);

    <T> void viewClass(Class<T> viewClass);

    void setTooManyRowsReturnedThreshold(int tooManyRowsReturnedThreshold);

    void slowOperationThreshold(Duration threshold);
}
