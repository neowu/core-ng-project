package core.framework.impl.search;

import com.carrotsearch.hppc.cursors.ObjectObjectCursor;
import core.framework.search.ElasticSearch;
import core.framework.search.ElasticSearchIndex;
import core.framework.search.ElasticSearchType;
import core.framework.search.SearchException;
import core.framework.util.Lists;
import core.framework.util.StopWatch;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.cluster.state.ClusterStateRequest;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.network.NetworkService;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
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
    public Duration slowOperationThreshold = Duration.ofSeconds(5);
    public Duration timeout = Duration.ofSeconds(10);
    public boolean sniff;      // if enabled, es client will use all nodes in cluster and only use "publish address" to connect
    private Client client;

    public void host(String host) {
        addresses.add(new TransportAddress(new InetSocketAddress(host, 9300)));
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
            client().admin().indices().prepareCreate(index).setSource(new BytesArray(source), XContentType.JSON).get();
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
            client().admin().indices().preparePutTemplate(name).setSource(new BytesArray(source), XContentType.JSON).get();
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
            client().admin().indices().prepareFlush(index).get();
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
            client().admin().indices().prepareClose(index).get();
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
            client().admin().indices().prepareDelete(index).get();
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
            AdminClient adminClient = client().admin();
            ClusterStateResponse response = adminClient.cluster().state(new ClusterStateRequest().clear().metaData(true)).actionGet();
            ImmutableOpenMap<String, IndexMetaData> indices = response.getState().getMetaData().indices();
            List<ElasticSearchIndex> results = new ArrayList<>(indices.size());
            for (ObjectObjectCursor<String, IndexMetaData> cursor : indices) {
                IndexMetaData metaData = cursor.value;
                ElasticSearchIndex index = new ElasticSearchIndex();
                index.index = metaData.getIndex().getName();
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
        if (addresses.isEmpty()) throw new Error("addresses must not be empty");
        StopWatch watch = new StopWatch();
        try {
            Settings.Builder settings = Settings.builder();
            settings.put(NetworkService.TCP_CONNECT_TIMEOUT.getKey(), new TimeValue(timeout.toMillis()))
                    .put(TransportClient.CLIENT_TRANSPORT_PING_TIMEOUT.getKey(), new TimeValue(timeout.toMillis()))
                    .put(TransportClient.CLIENT_TRANSPORT_PING_TIMEOUT.getKey(), new TimeValue(timeout.toMillis()))
                    .put(TransportClient.CLIENT_TRANSPORT_IGNORE_CLUSTER_NAME.getKey(), "true");     // refer to https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/transport-client.html
            if (sniff) {
                settings.put(TransportClient.CLIENT_TRANSPORT_SNIFF.getKey(), true);
            }
            TransportClient client = new PreBuiltTransportClient(settings.build());
            addresses.forEach(client::addTransportAddress);
            return client;
        } finally {
            logger.info("create elasticsearch client, addresses={}, elapsedTime={}", addresses, watch.elapsedTime());
        }
    }

    Client client() {
        if (client == null) initialize();
        return client;
    }
}
