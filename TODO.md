### ideas
* cache: advanced feature: local/remote 2 level, async get on expiration, pre warm? ben-manes/caffeine
* redis: support cluster?
* inject: remove method inject support? 
* template, use ByteBuffer[] for performance?
* general retry and throttling?
* mongo: collect mongo stats thru JMX ConnectionPoolStatisticsMBean?
* framework error (queue listener, background task error, customer scheduler trigger) forward to kafka?
* cache: hit rate report?
* streaming, data pipeline? (kafka stream/beam/google dataflow) 

* http: wait java11 with http2 client? OkHttp's POST is 5x slower than apache http client 
* ws: provide way to handler json command with validation?
* kafka: unique groupId to listen all messages for ws
* http: rewrite path parsing for better speed?
* rewrite all equals/hashcode

### jdk 9/10 incompatible list
* gradle: gradle doesn't support java module as first class yet, https://guides.gradle.org/building-java-9-modules/
* spotbugs: OBL bug https://github.com/spotbugs/spotbugs/issues/432  
* elasticsearch libs has too many duplicated namespaces with module-info.java enabled
* class in interface module generates Validator make interface depends on core-ng module (java module)
