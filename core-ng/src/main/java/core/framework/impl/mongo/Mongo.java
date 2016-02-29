package core.framework.impl.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import core.framework.api.mongo.Collection;
import core.framework.api.mongo.MongoCollection;
import core.framework.api.util.Exceptions;
import core.framework.api.util.StopWatch;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * @author neo
 */
public class Mongo {
    final EntityCodecs codecs = new EntityCodecs();
    private final Logger logger = LoggerFactory.getLogger(Mongo.class);
    private final MongoClientOptions.Builder builder = MongoClientOptions.builder().socketKeepAlive(true);
    int timeoutInMs = (int) Duration.ofSeconds(10).toMillis();
    int tooManyRowsReturnedThreshold = 2000;
    long slowOperationThresholdInMs = Duration.ofSeconds(5).toMillis();

    private MongoClientURI uri;
    private MongoClient mongoClient;
    private MongoDatabase database;

    public void initialize() {
        StopWatch watch = new StopWatch();
        try {
            if (uri == null) throw new Error("uri() must be called before initialize");
            database = createDatabase(uri, codecRegistry());
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
        logger.info("close mongodb client, uri={}", uri);
        mongoClient.close();
    }

    private CodecRegistry codecRegistry() {
        List<Codec<?>> codecs = new ArrayList<>(this.codecs.codecs.values());
        codecs.add(new LocalDateTimeCodec());
        return CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(), CodecRegistries.fromCodecs(codecs));
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
        slowOperationThresholdInMs = threshold.toMillis();
    }

    <T> com.mongodb.client.MongoCollection<T> mongoCollection(Class<T> entityClass) {
        Collection collection = entityClass.getDeclaredAnnotation(Collection.class);
        if (database == null) initialize(); // lazy init for dev test, initialize will be called in startup hook for complete env
        return database.getCollection(collection.name(), entityClass);
    }
}
