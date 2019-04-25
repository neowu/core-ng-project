package core.framework.mongo.module;

import com.mongodb.ConnectionString;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.module.ShutdownHook;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

import java.net.InetSocketAddress;

/**
 * @author neo
 */
public class TestMongoConfig extends MongoConfig {
    private static MongoServer server;
    String name;

    @Override
    protected void initialize(ModuleContext context, String name) {
        super.initialize(context, name);
        this.name = name;

        startInMemoryMongoServer(context);
    }

    private void startInMemoryMongoServer(ModuleContext context) {
        // in test env, config is initialized in order and within same thread, so no threading issue
        if (server == null) {
            server = new MongoServer(new MemoryBackend());
            server.bind(new InetSocketAddress("localhost", 27017));
            context.shutdownHook.add(ShutdownHook.STAGE_7, timeout -> server.shutdown());
        }
    }

    @Override
    ConnectionString connectionString(ConnectionString uri) {
        String database = name == null ? "test" : name;
        return new ConnectionString("mongodb://localhost:27017/" + database);
    }
}
