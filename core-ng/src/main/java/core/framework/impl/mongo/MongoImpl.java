package core.framework.impl.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import core.framework.api.mongo.Collection;
import core.framework.api.mongo.Mongo;
import core.framework.api.mongo.MongoCollection;
import core.framework.api.util.Exceptions;
import core.framework.api.util.StopWatch;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * @author neo
 */
public class MongoImpl implements Mongo {
    final EntityCodecs codecs = new EntityCodecs();
    private final Logger logger = LoggerFactory.getLogger(MongoImpl.class);
    private final MongoClientOptions.Builder builder = MongoClientOptions.builder()
        .socketKeepAlive(true)
        .cursorFinalizerEnabled(false); // framework always close db cursor
    int timeoutInMs = (int) Duration.ofSeconds(10).toMillis();
    int tooManyRowsReturnedThreshold = 2000;
    long slowOperationThresholdInNanos = Duration.ofSeconds(5).toNanos();

    private MongoClientURI uri;
    private MongoClient mongoClient;
    private MongoDatabase database;

    public void initialize() {
        StopWatch watch = new StopWatch();
        try {
            if (uri == null) throw new Error("uri() must be called before initialize");
            CodecRegistry registry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(), codecs.codecRegistry());
            database = createDatabase(uri, registry);
        } finally {
            logger.info("initialize mongo client, uri={}, elapsedTime={}", uri, watch.elapsedTime());
        }
    }

    protected MongoDatabase createDatabase(MongoClientURI uri, CodecRegistry registry) {
        builder.connectTimeout(timeoutInMs);
        builder.socketTimeout(timeoutInMs);
        builder.codecRegistry(registry);
        mongoClient = new MongoClient(uri);
        return mongoClient.getDatabase(uri.getDatabase());
    }

    public void close() {
        if (mongoClient != null) {  // if app didn't call createDatabase, then mongoClient will be null
            logger.info("close mongodb client, uri={}", uri);
            mongoClient.close();
        }
    }

    @Override
    public void dropCollection(String collection) {
        StopWatch watch = new StopWatch();
        try {
            database().getCollection(collection).drop();
        } finally {
            logger.info("dropCollection, collection={}, elapsedTime={}", collection, watch.elapsedTime());
        }
    }

    public void uri(String uri) {
        this.uri = new MongoClientURI(uri, builder);
        if (this.uri.getDatabase() == null) throw Exceptions.error("uri must have database, uri={}", uri);
    }

    public void poolSize(int minSize, int maxSize) {
        if (database != null) throw new Error("poolSize() must be called before initialize");
        builder.minConnectionsPerHost(minSize);
        builder.connectionsPerHost(maxSize);
    }

    public final void timeout(Duration timeout) {
        if (database != null) throw new Error("timeout() must be called before initialize");
        timeoutInMs = (int) timeout.toMillis();
    }

    public <T> MongoCollection<T> collection(Class<T> entityClass) {
        if (database != null) throw new Error("collection() must be called before initialize");
        new EntityClassValidator(entityClass).validateEntityClass();
        codecs.registerEntity(entityClass);
        return new MongoCollectionImpl<>(this, entityClass);
    }

    public <T> void view(Class<T> viewClass) {
        if (database != null) throw new Error("view() must be called before initialize");
        new EntityClassValidator(viewClass).validateViewClass();
        codecs.registerView(viewClass);
    }

    public void tooManyRowsReturnedThreshold(int tooManyRowsReturnedThreshold) {
        if (database != null) throw new Error("tooManyRowsReturnedThreshold() must be called before initialize");
        this.tooManyRowsReturnedThreshold = tooManyRowsReturnedThreshold;
    }

    public void slowOperationThreshold(Duration threshold) {
        if (database != null) throw new Error("slowOperationThreshold() must be called before initialize");
        slowOperationThresholdInNanos = threshold.toNanos();
    }

    <T> com.mongodb.client.MongoCollection<T> mongoCollection(Class<T> entityClass) {
        Collection collection = entityClass.getDeclaredAnnotation(Collection.class);
        return database().getCollection(collection.name(), entityClass);
    }

    private MongoDatabase database() {
        if (database == null) initialize(); // lazy init for dev test, initialize will be called in startup hook for complete env
        return database;
    }
}
