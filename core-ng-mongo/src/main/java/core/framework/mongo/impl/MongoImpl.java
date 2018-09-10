package core.framework.mongo.impl;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import core.framework.mongo.Collection;
import core.framework.mongo.Mongo;
import core.framework.mongo.MongoCollection;
import core.framework.util.StopWatch;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class MongoImpl implements Mongo {
    final EntityCodecs codecs = new EntityCodecs();
    private final Logger logger = LoggerFactory.getLogger(MongoImpl.class);
    private final MongoClientOptions.Builder builder = MongoClientOptions.builder()
                                                                         .maxConnectionIdleTime((int) Duration.ofMinutes(30).toMillis())
                                                                         .cursorFinalizerEnabled(false); // framework always close db cursor
    public int tooManyRowsReturnedThreshold = 2000;
    int timeoutInMs = (int) Duration.ofSeconds(15).toMillis();
    long slowOperationThresholdInNanos = Duration.ofSeconds(5).toNanos();
    CodecRegistry registry;
    private MongoClientURI uri;
    private MongoClient mongoClient;
    private MongoDatabase database;

    public void initialize() {
        registry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(), codecs.codecRegistry());
        database = createDatabase(registry);
    }

    MongoDatabase createDatabase(CodecRegistry registry) {
        if (uri == null) throw new Error("uri must not be null");
        var watch = new StopWatch();
        try {
            builder.connectTimeout(timeoutInMs);
            builder.socketTimeout(timeoutInMs);
            builder.maxWaitTime(timeoutInMs);   // pool checkout timeout
            builder.serverSelectionTimeout(timeoutInMs * 3);    // able to try 3 servers
            builder.codecRegistry(registry);
            mongoClient = new MongoClient(uri);
            return mongoClient.getDatabase(uri.getDatabase());
        } finally {
            logger.info("create mongo client, uri={}, elapsed={}", uri, watch.elapsed());
        }
    }

    public void close() {
        if (mongoClient == null)
            return;   // if app didn't call createDatabase, e.g. failed on previous startup hook, or unit test, then mongoClient will be null

        logger.info("close mongodb client, uri={}", uri);
        mongoClient.close();
    }

    @Override
    public void dropCollection(String collection) {
        var watch = new StopWatch();
        try {
            database().getCollection(collection).drop();
        } finally {
            logger.info("dropCollection, collection={}, elapsed={}", collection, watch.elapsed());
        }
    }

    public void uri(String uri) {
        this.uri = new MongoClientURI(uri, builder);
        if (this.uri.getDatabase() == null) throw new Error(format("uri must have database, uri={}", uri));
    }

    public void poolSize(int minSize, int maxSize) {
        builder.minConnectionsPerHost(minSize);
        builder.connectionsPerHost(maxSize);
    }

    public final void timeout(Duration timeout) {
        timeoutInMs = (int) timeout.toMillis();
    }

    public <T> MongoCollection<T> collection(Class<T> entityClass) {
        var watch = new StopWatch();
        try {
            new MongoClassValidator(entityClass).validateEntityClass();
            codecs.registerEntity(entityClass);
            return new MongoCollectionImpl<>(this, entityClass);
        } finally {
            logger.info("register mongo entity, entityClass={}, elapsed={}", entityClass.getCanonicalName(), watch.elapsed());
        }
    }

    public <T> void view(Class<T> viewClass) {
        var watch = new StopWatch();
        try {
            new MongoClassValidator(viewClass).validateViewClass();
            codecs.registerView(viewClass);
        } finally {
            logger.info("register mongo view, viewClass={}, elapsed={}", viewClass.getCanonicalName(), watch.elapsed());
        }
    }

    public void slowOperationThreshold(Duration threshold) {
        slowOperationThresholdInNanos = threshold.toNanos();
    }

    <T> com.mongodb.client.MongoCollection<T> mongoCollection(Class<T> entityClass) {
        Collection collection = entityClass.getDeclaredAnnotation(Collection.class);
        return database().getCollection(collection.name(), entityClass);
    }

    private MongoDatabase database() {
        if (database == null) initialize(); // lazy init for dev/test, initialize will be called in startup hook on server env
        return database;
    }
}
