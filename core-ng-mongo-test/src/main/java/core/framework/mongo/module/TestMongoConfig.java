package core.framework.mongo.module;

import com.mongodb.ConnectionString;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.module.ShutdownHook;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author neo
 */
public class TestMongoConfig extends MongoConfig {
    private static final AtomicInteger NEXT_MONGO_SERVER_PORT = new AtomicInteger(27017);
    int port;

    @Override
    protected void initialize(ModuleContext context, String name) {
        super.initialize(context, name);
        var server = new MongoServer(new MemoryBackend());
        port = NEXT_MONGO_SERVER_PORT.getAndIncrement();
        server.bind(new InetSocketAddress("localhost", port));
        context.shutdownHook.add(ShutdownHook.STAGE_7, timeout -> server.shutdown());
    }

    @Override
    ConnectionString connectionString(ConnectionString uri) {
        return new ConnectionString("mongodb://localhost:" + port + "/test");
    }
}
