package core.framework.impl.search;

import core.framework.api.search.ElasticSearchType;
import core.framework.api.search.Index;
import core.framework.api.search.SearchException;
import core.framework.api.util.Lists;
import core.framework.api.util.StopWatch;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.network.NetworkService;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

/**
 * @author neo
 */
public final class ElasticSearch {
    private final Logger logger = LoggerFactory.getLogger(ElasticSearch.class);
    private final List<TransportAddress> remoteAddresses = Lists.newArrayList();
    private Path localHomePath;
    private Duration timeout = Duration.ofSeconds(10);
    private Duration slowQueryThreshold = Duration.ofSeconds(5);

    private Client client;

    public void remote(String host) {
        if (client != null) throw new Error("remote() must be called before creating client");
        remoteAddresses.add(new InetSocketTransportAddress(new InetSocketAddress(host, 9300)));
    }

    public void local(Path localHomePath) {
        if (client != null) throw new Error("local() must be called before creating client");
        this.localHomePath = localHomePath;
    }

    public void slowQueryThreshold(Duration slowQueryThreshold) {
        if (client != null) throw new Error("slowQueryThreshold() must be called before creating client");
        this.slowQueryThreshold = slowQueryThreshold;
    }

    public void timeout(Duration timeout) {
        if (client != null) throw new Error("timeout() must be called before creating client");
        this.timeout = timeout;
    }

    public <T> ElasticSearchType<T> type(Class<T> documentClass) {
        new DocumentClassValidator(documentClass).validate();
        Index index = documentClass.getDeclaredAnnotation(Index.class);
        return new ElasticSearchTypeImpl<>(client(), index.index(), index.type(), documentClass, slowQueryThreshold);
    }

    public void close() {
        if (client == null) return;

        logger.info("close elastic search client");
        try {
            client.close();
        } catch (ElasticsearchException e) {
            logger.warn("failed to close elastic search client", e);
        }
    }

    public void createIndex(String index, String source) {
        StopWatch watch = new StopWatch();
        try {
            client().admin()
                .indices()
                .prepareCreate(index)
                .setSource(source)
                .get();
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            logger.debug("create index, index={}, elapsedTime={}", index, watch.elapsedTime());
        }
    }

    public void flush(String index) {
        StopWatch watch = new StopWatch();
        try {
            client().admin()
                .indices()
                .prepareFlush(index)
                .get();
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            logger.debug("flush, index={}, elapsedTime={}", index, watch.elapsedTime());
        }
    }

    private Client client() {
        if (client == null) {
            if (!remoteAddresses.isEmpty()) {
                client = createRemoteElasticSearch();
            } else if (localHomePath != null) {
                client = createLocalElasticSearch();
            } else {
                throw new Error("failed to create elastic search client, please specify either remote or local");
            }
        }
        return client;
    }

    private Client createLocalElasticSearch() {
        StopWatch watch = new StopWatch();
        try {
            Settings.Builder settings = Settings.settingsBuilder()
                .put("http.enabled", "false")       // refer to org.elasticsearch.node.Node.start()
                .put("script.inline", "on")         // refer to https://www.elastic.co/guide/en/elasticsearch/reference/current/_setting_changes.html#migration-script-settings
                .put("path.home", localHomePath);   // refer to org.elasticsearch.env.Environment.Environment()
            Node node = NodeBuilder.nodeBuilder().settings(settings).local(true).node();
            return node.client();
        } finally {
            logger.info("create local elastic search client, dataPath={}, elapsedTime={}", localHomePath, watch.elapsedTime());
        }
    }

    private Client createRemoteElasticSearch() {
        StopWatch watch = new StopWatch();
        try {
            Settings.Builder settings = Settings.settingsBuilder()
                .put(NetworkService.TcpSettings.TCP_CONNECT_TIMEOUT, new TimeValue(timeout.toMillis()))
                .put("client.transport.ignore_cluster_name", "true");     // refer to https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/transport-client.html
            TransportClient client = TransportClient.builder().settings(settings).build();
            remoteAddresses.forEach(client::addTransportAddress);
            return client;
        } finally {
            logger.info("create remote elastic search client, addresses={}, elapsedTime={}", remoteAddresses, watch.elapsedTime());
        }
    }
}
