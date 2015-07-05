package core.framework.api.search;

import core.framework.api.util.Lists;
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

    Path localDataPath;
    List<TransportAddress> remoteAddresses = Lists.newArrayList();
    Duration timeout = Duration.ofSeconds(10);
    Duration slowQueryThreshold = Duration.ofSeconds(5);
    String index = "main";

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

    public ElasticSearchBuilder index(String index) {
        this.index = index;
        return this;
    }

    public ElasticSearchBuilder timeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    @Override
    public ElasticSearch get() {
        Client client;
        if (!remoteAddresses.isEmpty()) {
            logger.info("create remote elastic search client, addresses={}", remoteAddresses);
            ImmutableSettings.Builder settings = ImmutableSettings.settingsBuilder()
                .put(NetworkService.TcpSettings.TCP_CONNECT_TIMEOUT, timeout.toMillis());
            client = new TransportClient(settings);
            remoteAddresses.forEach(((TransportClient) client)::addTransportAddress);
        } else if (localDataPath != null) {
            logger.info("create local elastic search client, dataPath={}", localDataPath);
            ImmutableSettings.Builder settings = ImmutableSettings.settingsBuilder()
                .put("http.enabled", "false")
                .put("path.data", localDataPath);

            Node node = nodeBuilder().settings(settings).local(true).node();
            client = node.client();
        } else {
            throw new Error("failed to build elastic search client, please specify remote or local");
        }

        return new ElasticSearch(client, index, slowQueryThreshold);
    }
}
