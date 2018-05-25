### ideas
* cache: advanced feature: local/remote 2 level, async get on expiration, pre warm? ben-manes/caffeine
* http: wait java9 with http2 client? OkHttp's POST is 5x slower than apache http client 
* search: elasticsearch plan to use java high level client to replace current transport client, wait until complete and migrate to "org.elasticsearch.client:elasticsearch-rest-high-level-client:${elasticVersion}"
* redis: support cluster?
* inject: remove method inject support? 
* template, use ByteBuffer[] for performance?
* general retry and throttling?
* webservice/redis/mongo client: client retry on network issue?
* validator: annotation for website, like @HTMLSafeString?
* mongo: collect mongo stats thru JMX ConnectionPoolStatisticsMBean?
* framework error (queue listener, background task error, customer scheduler trigger) forward to kafka?
* support websocket?

### jdk 9/10 incompatible list
* gradle: gradle doesn't support java module as first class yet, https://guides.gradle.org/building-java-9-modules/
* spotbugs: OBL bug https://github.com/spotbugs/spotbugs/issues/432  
* elasticsearch libs has too many duplicated namespaces with module-info.java enabled
* class in interface module generates Validator make interface depends on core-ng module (java module)
