package core.framework.impl.search;

import com.carrotsearch.hppc.cursors.ObjectObjectCursor;
import core.framework.api.search.ElasticSearch;
import core.framework.api.search.ElasticSearchIndex;
import core.framework.api.search.ElasticSearchType;
import core.framework.api.search.SearchException;
import core.framework.api.util.Lists;
import core.framework.api.util.StopWatch;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.cluster.state.ClusterStateRequest;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.network.NetworkService;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * @author neo
 */
public class ElasticSearchImpl implements ElasticSearch {
    private final Logger logger = LoggerFactory.getLogger(ElasticSearchImpl.class);
    private final List<TransportAddress> addresses = Lists.newArrayList();
    Client client;
    private Duration timeout = Duration.ofSeconds(10);
    private Duration slowOperationThreshold = Duration.ofSeconds(5);

    public void host(String host) {
        addresses.add(new InetSocketTransportAddress(new InetSocketAddress(host, 9300)));
    }

    public void slowOperationThreshold(Duration slowOperationThreshold) {
        this.slowOperationThreshold = slowOperationThreshold;
    }

    public void timeout(Duration timeout) {
        this.timeout = timeout;
    }

    public void initialize() {
        client = createClient();
    }

    public <T> ElasticSearchType<T> type(Class<T> documentClass) {
        StopWatch watch = new StopWatch();
        try {
            new DocumentClassValidator(documentClass).validate();
            return new ElasticSearchTypeImpl<>(this, documentClass, slowOperationThreshold);
        } finally {
            logger.info("register elasticsearch type, documentClass={}, elapsedTime={}", documentClass.getCanonicalName(), watch.elapsedTime());
        }
    }

    public void close() {
        if (client == null) return;

        logger.info("close elasticsearch client");
        try {
            client.close();
        } catch (ElasticsearchException e) {
            logger.warn("failed to close elastic search client", e);
        }
    }

    @Override
    public void createIndex(String index, String source) {
        StopWatch watch = new StopWatch();
        try {
            client.admin().indices().prepareCreate(index).setSource(source).get();
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            logger.info("create index, index={}, elapsedTime={}", index, watch.elapsedTime());
        }
    }

    @Override
    public void createIndexTemplate(String name, String source) {
        StopWatch watch = new StopWatch();
        try {
            client.admin().indices().preparePutTemplate(name).setSource(source).get();
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            logger.info("create index template, name={}, elapsedTime={}", name, watch.elapsedTime());
        }
    }

    @Override
    public void flush(String index) {
        StopWatch watch = new StopWatch();
        try {
            client.admin().indices().prepareFlush(index).get();
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            logger.info("flush, index={}, elapsedTime={}", index, watch.elapsedTime());
        }
    }

    @Override
    public void closeIndex(String index) {
        StopWatch watch = new StopWatch();
        try {
            client.admin().indices().prepareClose(index).get();
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            logger.info("close, index={}, elapsedTime={}", index, watch.elapsedTime());
        }
    }

    @Override
    public void deleteIndex(String index) {
        StopWatch watch = new StopWatch();
        try {
            client.admin().indices().prepareDelete(index).get();
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            logger.info("delete, index={}, elapsedTime={}", index, watch.elapsedTime());
        }
    }

    @Override
    public List<ElasticSearchIndex> indices() {
        StopWatch watch = new StopWatch();
        try {
            AdminClient adminClient = client.admin();
            ClusterStateResponse response = adminClient.cluster().state(new ClusterStateRequest().clear().metaData(true)).actionGet();
            ImmutableOpenMap<String, IndexMetaData> indices = response.getState().getMetaData().indices();
            List<ElasticSearchIndex> results = new ArrayList<>(indices.size());
            for (ObjectObjectCursor<String, IndexMetaData> cursor : indices) {
                IndexMetaData metaData = cursor.value;
                ElasticSearchIndex index = new ElasticSearchIndex();
                index.index = metaData.getIndex();
                index.state = metaData.getState();
                results.add(index);
            }
            return results;
        } catch (ElasticsearchException e) {
            throw new SearchException(e);   // due to elastic search uses async executor to run, we have to wrap the exception to retain the original place caused the exception
        } finally {
            logger.info("indices, elapsedTime={}", watch.elapsedTime());
        }
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
