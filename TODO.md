### ideas
* cache: advanced feature: local/remote 2 level, async get on expiration, pre warm? ben-manes/caffeine
* http: wait java9 with http2 client? OkHttp's POST is 5x slower than apache http client 
* search: elasticsearch plan to use java high level client to replace current transport client, wait until complete and migrate to "org.elasticsearch.client:elasticsearch-rest-high-level-client:${elasticVersion}"
* jdk9: split 3rd party product into modules? (e.g. mongo/es/kafka/db)
* redis: support cluster?
* inject: remove method inject support? 
* template, use ByteBuffer[] for performance tuning?
* general retry and throttling?
* webservice/redis/mongo client: client retry on network issue?
* validator: annotation for website, like @HTMLSafeString?
* mongo: collect mongo stats thru JMX ConnectionPoolStatisticsMBean
* framework error, queue listener, background task error forward to kafka?
* long running thread support or needed?
* missing @PathParam, validation error not clear?
* support websocket?

### jdk 9 incompatible list
* spotbugs: OBL bug https://github.com/spotbugs/spotbugs/issues/432  
* elasticsearch libs has too many duplicated namespaces with module-info.java enabled
* alpine jdk 9 is not released yet, docker openjdk:jre-alpine image size is much smaller

### jdk 10
* remove core.framework.util.Threads.availableProcessors, check default behavior and -XX:ActiveProcessorCount
