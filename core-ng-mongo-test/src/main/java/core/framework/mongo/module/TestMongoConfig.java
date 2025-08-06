package core.framework.mongo.module;

import com.mongodb.ConnectionString;
import core.framework.internal.module.ModuleContext;
import core.framework.internal.module.ShutdownHook;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.jspecify.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author neo
 */
public class TestMongoConfig extends MongoConfig {
    private static final ReentrantLock LOCK = new ReentrantLock();

    // only start one mongo server for testing to reduce resource overhead,
    // only breaking case is that multiple mongo() using same collection name, then if one unit test operates both MongoCollection may result in conflict or merged results
    // this can be avoided by designing test differently
    @Nullable
    private static InetSocketAddress localMongoAddress;
    @Nullable
    String name;

    @Override
    protected void initialize(ModuleContext context, @Nullable String name) {
        super.initialize(context, name);
        this.name = name;

        startLocalMongoServer(context);
    }

    private void startLocalMongoServer(ModuleContext context) {
        LOCK.lock();
        try {
            // in test env, config is initialized in order and within same thread, so no threading issue
            if (localMongoAddress == null) {
                var server = new MongoServer(new MemoryBackend());
                localMongoAddress = server.bind();
                context.shutdownHook.add(ShutdownHook.STAGE_6, timeout -> server.shutdown());
            }
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    ConnectionString connectionString(ConnectionString uri) {
        return connectionString(Objects.requireNonNull(localMongoAddress).getPort());
    }

    ConnectionString connectionString(int port) {
        String database = name == null ? "test" : name;
        return new ConnectionString("mongodb://localhost:" + port + "/" + database);
    }
}
