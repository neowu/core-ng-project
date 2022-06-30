package core.framework.mongo.impl;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.connection.ClusterSettings;
import com.mongodb.connection.ConnectionPoolSettings;
import com.mongodb.connection.SocketSettings;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import core.framework.internal.log.PerformanceWarning;
import core.framework.mongo.Collection;
import core.framework.mongo.Mongo;
import core.framework.mongo.MongoCollection;
import core.framework.util.StopWatch;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
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
        .maxConnectionIdleTime(Duration.ofMinutes(30).toMillis(), TimeUnit.MILLISECONDS);
    private final PerformanceWarning warning = new PerformanceWarning("mongo", 2000, Duration.ofSeconds(5), 2000, 10_000, 10_000);

    public ConnectionString uri;
    long timeoutInMs = Duration.ofSeconds(15).toMillis();
    CodecRegistry registry;
    private MongoClient mongoClient;
    private MongoDatabase database;

    // initialize will be called in startup hook, no need to synchronize
    public void initialize() {
        // mongo client could be used by test context directly (e.g. get bean(Mongo.class) then init data)
        // to handle initializing multiply times, this is still better than lazy initialize for prod runtime env
        if (database == null) {
            // put our own codec before mongo default codec, as override mongo default enum codec, refer to com.mongodb.MongoClientSettings.DEFAULT_CODEC_REGISTRY
            registry = CodecRegistries.fromRegistries(codecs.codecRegistry(), MongoClientSettings.getDefaultCodecRegistry());
            database = createDatabase(registry);
        }
    }

    private MongoDatabase createDatabase(CodecRegistry registry) {
        if (uri == null) throw new Error("uri must not be null");
        String database = uri.getDatabase();
        if (database == null) throw new Error("uri must have database, uri=" + uri);
        var watch = new StopWatch();
        try {
            connectionPoolSettings.maxWaitTime(timeoutInMs, TimeUnit.MILLISECONDS); // pool checkout timeout
            var socketSettings = SocketSettings.builder()
                .connectTimeout((int) timeoutInMs, TimeUnit.MILLISECONDS)
                .readTimeout((int) timeoutInMs, TimeUnit.MILLISECONDS)
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
    public void createIndex(String collection, Bson keys, IndexOptions options) {
        var watch = new StopWatch();
        try {
            database.getCollection(collection).createIndex(keys, options);
        } finally {
            logger.info("createIndex, collection={}, keys={}, options={}, elapsed={}", collection, keys, options, watch.elapsed());
        }
    }

    @Override
    public void dropCollection(String collection) {
        var watch = new StopWatch();
        try {
            database.getCollection(collection).drop();
        } finally {
            logger.info("dropCollection, collection={}, elapsed={}", collection, watch.elapsed());
        }
    }

    @Override
    public Document runCommand(Bson command) {
        var watch = new StopWatch();
        try {
            return database.runCommand(command);
        } finally {
            logger.info("runCommand, command={}, elapsed={}", command, watch.elapsed());
        }
    }

    public void poolSize(int minSize, int maxSize) {
        connectionPoolSettings.minSize(minSize);
        connectionPoolSettings.maxSize(maxSize);
    }

    public final void timeout(Duration timeout) {
        timeoutInMs = timeout.toMillis();
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

    <T> com.mongodb.client.MongoCollection<T> mongoCollection(Class<T> entityClass) {
        Collection collection = entityClass.getDeclaredAnnotation(Collection.class);
        return database.getCollection(collection.name(), entityClass);
    }

    void track(long elapsed, int readEntries, int writeEntries) {
        ActionLog actionLog = LogManager.CURRENT_ACTION_LOG.get();
        if (actionLog != null) {
            actionLog.track(warning.operation, elapsed, readEntries, writeEntries, warning);
        }
    }
}
