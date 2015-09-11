package core.framework.api.search;

import core.framework.api.util.Lists;
import core.framework.api.util.StopWatch;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.network.NetworkService;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 * @author neo
 */
public final class ElasticSearchBuilder implements Supplier<ElasticSearch> {
    private final Logger logger = LoggerFactory.getLogger(ElasticSearchBuilder.class);

    private Path localDataPath;
    private final List<TransportAddress> remoteAddresses = Lists.newArrayList();
    private Duration timeout = Duration.ofSeconds(10);
    private Duration slowQueryThreshold = Duration.ofSeconds(5);

    public ElasticSearchBuilder remote(String host) {
        remoteAddresses.add(new InetSocketTransportAddress(host, 9300));
        return this;
    }

    public ElasticSearchBuilder local(Path localDataPath) {
        this.localDataPath = localDataPath;
        return this;
    }

    public ElasticSearchBuilder slowQueryThreshold(Duration slowQueryThreshold) {
        this.slowQueryThreshold = slowQueryThreshold;
        return this;
    }

    public ElasticSearchBuilder timeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    @Override
    public ElasticSearch get() {
        if (!remoteAddresses.isEmpty()) {
            return createRemoteElasticSearch();
        } else if (localDataPath != null) {
            return createLocalElasticSearch();
        } else {
            throw new Error("failed to build elastic search client, please specify either remote or local");
        }
    }

    private ElasticSearch createLocalElasticSearch() {
        StopWatch watch = new StopWatch();
        try {
            ImmutableSettings.Builder settings = ImmutableSettings.settingsBuilder()
                .put("http.enabled", "false")
                .put("script.groovy.sandbox.enabled", "true")
                .put("path.data", localDataPath);
            Node node = nodeBuilder().settings(settings).local(true).node();
            Client client = node.client();
            return new ElasticSearch(client, slowQueryThreshold);
        } finally {
            logger.info("create local elastic search client, dataPath={}, elapsedTime={}", localDataPath, watch.elapsedTime());
        }
    }

    private ElasticSearch createRemoteElasticSearch() {
        StopWatch watch = new StopWatch();
        try {
            ImmutableSettings.Builder settings = ImmutableSettings.settingsBuilder()
                .put(NetworkService.TcpSettings.TCP_CONNECT_TIMEOUT, timeout.toMillis());
            Client client = new TransportClient(settings);
            remoteAddresses.forEach(((TransportClient) client)::addTransportAddress);
            return new ElasticSearch(client, slowQueryThreshold);
        } finally {
            logger.info("create remote elastic search client, addresses={}, elapsedTime={}", remoteAddresses, watch.elapsedTime());
        }
    }
}
