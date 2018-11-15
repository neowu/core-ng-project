package core.framework.mongo.impl;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.connection.ClusterSettings;
import com.mongodb.connection.ConnectionPoolSettings;
import com.mongodb.connection.SocketSettings;
import core.framework.impl.log.LogManager;
import core.framework.mongo.Collection;
import core.framework.mongo.Mongo;
import core.framework.mongo.MongoCollection;
import core.framework.util.StopWatch;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author neo
 */
public class MongoImpl implements Mongo {
    final EntityCodecs codecs = new EntityCodecs();
    private final Logger logger = LoggerFactory.getLogger(MongoImpl.class);
    private final ConnectionPoolSettings.Builder connectionPoolSettings = ConnectionPoolSettings.builder()
                                                                                                .maxConnectionIdleTime((int) Duration.ofMinutes(30).toMillis(), TimeUnit.MILLISECONDS);
    public ConnectionString uri;
    public int tooManyRowsReturnedThreshold = 2000;
    int timeoutInMs = (int) Duration.ofSeconds(15).toMillis();
    long slowOperationThresholdInNanos = Duration.ofSeconds(5).toNanos();
    CodecRegistry registry;
    private MongoClient mongoClient;
    private MongoDatabase database;

    public void initialize() {
        registry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), codecs.codecRegistry());
        database = createDatabase(registry);
    }

    MongoDatabase createDatabase(CodecRegistry registry) {
        if (uri == null) throw new Error("uri must not be null");
        String database = uri.getDatabase();
        if (database == null) throw new Error("uri must have database, uri=" + uri);
        var watch = new StopWatch();
        try {
            connectionPoolSettings.maxWaitTime(timeoutInMs, TimeUnit.MILLISECONDS); // pool checkout timeout
            var socketSettings = SocketSettings.builder()
                                               .connectTimeout(timeoutInMs, TimeUnit.MILLISECONDS)
                                               .readTimeout(timeoutInMs, TimeUnit.MILLISECONDS)
                                               .build();
            var clusterSettings = ClusterSettings.builder()
                                                 .serverSelectionTimeout(timeoutInMs * 3, TimeUnit.MILLISECONDS)    // able to try 3 servers
                                                 .build();
            var settings = MongoClientSettings.builder()
                                              .applicationName(LogManager.APP_NAME)
                                              .codecRegistry(registry)
                                              .applyToConnectionPoolSettings(builder -> builder.applySettings(connectionPoolSettings.build()))
                                              .applyToSocketSettings(builder -> builder.applySettings(socketSettings))
                                              .applyToClusterSettings(builder -> builder.applySettings(clusterSettings))
                                              .applyConnectionString(uri)
                                              .build();
            mongoClient = MongoClients.create(settings);
            return mongoClient.getDatabase(database);
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

    public void poolSize(int minSize, int maxSize) {
        connectionPoolSettings.minSize(minSize);
        connectionPoolSettings.maxSize(maxSize);
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
