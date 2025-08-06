package core.framework.search.module;

import core.framework.internal.module.ModuleContext;
import core.framework.internal.module.ShutdownHook;
import core.framework.search.impl.LocalElasticSearch;
import org.apache.http.HttpHost;
import org.elasticsearch.common.logging.LogConfigurator;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author neo
 */
public class TestSearchConfig extends SearchConfig {
    static {
        // org.apache.lucene.store.MemorySegmentIndexInputProvider <init>
        // INFO: Using MemorySegmentIndexInput with Java 21 or later; to disable start with -Dorg.apache.lucene.store.MMapDirectory.enableMemorySegments=false
        System.setProperty("org.apache.lucene.store.MMapDirectory.enableMemorySegments", "false");
    }

    private static final ReentrantLock LOCK = new ReentrantLock();

    // only start one local node for testing to reduce resource overhead,
    // only breaking case is that multiple search() using same index name, then if one unit test operates both ElasticSearchType will result in conflict or merged results
    // this can be avoided by designing test differently
    @Nullable
    private static HttpHost localESHost;

    @Override
    protected void initialize(ModuleContext context, @Nullable String name) {
        super.initialize(context, name);
        startLocalElasticSearch(context);
    }

    private void startLocalElasticSearch(ModuleContext context) {
        LOCK.lock();
        try {
            // in test env, config is initialized in order and within same thread, so no threading issue
            if (localESHost == null) {
                LogConfigurator.configureESLogging();

                var server = new LocalElasticSearch();
                localESHost = server.start();
                context.shutdownHook.add(ShutdownHook.STAGE_6, timeout -> server.close());
            }
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public void host(String host) {
        search.hosts = new HttpHost[]{localESHost};
    }

    @Override
    public void auth(String apiKeyId, String apiKeySecret) {
    }
}
