package core.framework.impl.search;

import core.framework.api.search.ElasticSearchType;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;

/**
 * @author neo
 */
public class ElasticSearch {
    private final Logger logger = LoggerFactory.getLogger(ElasticSearch.class);
    private final List<TransportAddress> addresses = Lists.newArrayList();
    private Duration timeout = Duration.ofSeconds(10);
    private Duration slowOperationThreshold = Duration.ofSeconds(5);

    private Client client;

    public void host(String host) {
        if (client != null) throw new Error("host() must be called before creating client");
        addresses.add(new InetSocketTransportAddress(new InetSocketAddress(host, 9300)));
    }

    public void slowOperationThreshold(Duration slowOperationThreshold) {
        if (client != null) throw new Error("slowOperationThreshold() must be called before creating client");
        this.slowOperationThreshold = slowOperationThreshold;
    }

    public void timeout(Duration timeout) {
        if (client != null) throw new Error("timeout() must be called before creating client");
        this.timeout = timeout;
    }

    public <T> ElasticSearchType<T> type(Class<T> documentClass) {
        new DocumentClassValidator(documentClass).validate();
        return new ElasticSearchTypeImpl<>(client(), documentClass, slowOperationThreshold);
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

    public void createIndexTemplate(String name, String source) {
        StopWatch watch = new StopWatch();
        try {
            client().admin()
                .indices()
                .preparePutTemplate(name)
                .setSource(source)
                .get();
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            logger.debug("create index template, name={}, elapsedTime={}", name, watch.elapsedTime());
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

    public Client client() {
        if (client == null) {
            client = createClient();
        }
        return client;
    }

    protected Client createClient() {
        if (addresses.isEmpty()) throw new Error("addresses must not be empty, please check config");
        StopWatch watch = new StopWatch();
        try {
            Settings.Builder settings = Settings.settingsBuilder()
                .put(NetworkService.TcpSettings.TCP_CONNECT_TIMEOUT, new TimeValue(timeout.toMillis()))
                .put("client.transport.ping_timeout", new TimeValue(timeout.toMillis()))
                .put("client.transport.ignore_cluster_name", "true");     // refer to https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/transport-client.html
            TransportClient client = TransportClient.builder().settings(settings).build();
            addresses.forEach(client::addTransportAddress);
            return client;
        } finally {
            logger.info("create elasticsearch client, addresses={}, elapsedTime={}", addresses, watch.elapsedTime());
        }
    }
}
