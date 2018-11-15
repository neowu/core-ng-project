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
    @Override
    protected void initialize(ModuleContext context, String name) {
        super.initialize(context, name);
        var server = new MongoServer(new MemoryBackend());
        server.bind(new InetSocketAddress("localhost", 27017));
        context.shutdownHook.add(ShutdownHook.STAGE_7, timeout -> server.shutdown());
    }

    @Override
    ConnectionString connectionString(ConnectionString uri) {
        return new ConnectionString("mongodb://localhost:27017/test");
    }
}
