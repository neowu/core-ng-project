## Change log

### 9.0.9 (3/20/2024 - )

* mysql: updated and patched to 8.3.0, fixed CJException should be wrapped as SQLException
  > make sure use "core.framework.mysql:mysql-connector-j:8.3.0-r2"
* thread: replace all synchronized with ReentrantLock/Condition, including RateControl/ShutdownHandling/Test
* http: convert some http error as warning
  > undertow "UT000133: Request did not contain an Upgrade header, upgrade is not permitted"
  > "response was sent, discard the current http transaction"
* search: update es to 8.13

### 9.0.8 (1/29/2024 - 3/7/2024)

* kafka: update to 3.7.0
  > update kafka docker demo with official image, refer to docker/kafka/docker-compose.yml
* message: change message listener FAILED_TO_STOP from warning to error
* executor: tweak shutdown handling, print all tasks not complete
* jre: published neowu/jre:21.0.2
* db: validate enum must have @Property for json field List<Enum>
  > to make it consistent with JSON serialization and ensure refactoring safety
* search: update es to 8.12.2
  > the JDK 21.0.2 issue is fixed
* virtualThread: update jdk.virtualThreadScheduler.parallelism to cpu * 8
* httpClient: revert okIO back to 3.2.0
  > okIO 3.3.0 may cause virtual thread deadlock with Http2Writer
  > will wait to see if future version JVM or okHTTP fix the issue
* undertow: revert back to 2.3.10
  > undertow 2.3.11 has memory leak issue with virtual thread, will keep eye on it
  > https://github.com/undertow-io/undertow/commit/c96363d683feb4b1066959d46be59cf2d59a7b7c

!!! okIO issue
https://github.com/square/okio/commit/f8434f575787198928a26334758ddbca9726b11c#diff-f63e8920e14cc4bf376a495cfcd1fbfa2eee7bbcdfec0ad10f2bc51237c59725
for some reason, Http2Writer.flush (synchronized) -> AsyncTimeout$Companion.cancelScheduledTimeout (changed to reentrantLock)
triggered VirtualThreads.park, make all other virtual threads which share same http2connection deadlocked

```
#20985 "kafka-listener-19336" virtual
      java.base/jdk.internal.misc.Unsafe.park(Native Method)
      java.base/java.lang.VirtualThread.parkOnCarrierThread(Unknown Source)
      java.base/java.lang.VirtualThread.park(Unknown Source)
      java.base/java.lang.System$2.parkVirtualThread(Unknown Source)
      java.base/jdk.internal.misc.VirtualThreads.park(Unknown Source)
      java.base/java.util.concurrent.locks.LockSupport.park(Unknown Source)
      java.base/java.util.concurrent.locks.AbstractQueuedSynchronizer.acquire(Unknown Source)
      java.base/java.util.concurrent.locks.AbstractQueuedSynchronizer.acquire(Unknown Source)
      java.base/java.util.concurrent.locks.ReentrantLock$Sync.lock(Unknown Source)
      java.base/java.util.concurrent.locks.ReentrantLock.lock(Unknown Source)
      okio.AsyncTimeout$Companion.cancelScheduledTimeout(AsyncTimeout.kt:273)
      okio.AsyncTimeout$Companion.access$cancelScheduledTimeout(AsyncTimeout.kt:204)
      okio.AsyncTimeout.exit(AsyncTimeout.kt:62)
      okio.AsyncTimeout$sink$1.write(AsyncTimeout.kt:341)
      okio.RealBufferedSink.flush(RealBufferedSink.kt:268)
      okhttp3.internal.http2.Http2Writer.flush(Http2Writer.kt:120)
      okhttp3.internal.http2.Http2Connection.flush(Http2Connection.kt:408)
      okhttp3.internal.http2.Http2Stream$FramingSink.close(Http2Stream.kt:624)
      okio.ForwardingSink.close(ForwardingSink.kt:37)
      okhttp3.internal.connection.Exchange$RequestBodySink.close(Exchange.kt:247)
      okio.RealBufferedSink.close(RealBufferedSink.kt:287)

// rest similar threads are all locked      
#20984 "kafka-listener-19335" virtual
      okhttp3.internal.http2.Http2Connection.newStream(Http2Connection.kt:240)
      okhttp3.internal.http2.Http2Connection.newStream(Http2Connection.kt:225)
      okhttp3.internal.http2.Http2ExchangeCodec.writeRequestHeaders(Http2ExchangeCodec.kt:76)
      okhttp3.internal.connection.Exchange.writeRequestHeaders(Exchange.kt:63)
#20974 "kafka-listener-19325" virtual
      okhttp3.internal.http2.Http2Writer.flush(Http2Writer.kt:119)
      okhttp3.internal.http2.Http2Connection.flush(Http2Connection.kt:408)
      okhttp3.internal.http2.Http2Stream$FramingSink.close(Http2Stream.kt:624)
      okio.ForwardingSink.close(ForwardingSink.kt:37)
      okhttp3.internal.connection.Exchange$RequestBodySink.close(Exchange.kt:247)
      okio.RealBufferedSink.close(RealBufferedSink.kt:287)
      okhttp3.internal.http.CallServerInterceptor.intercept(CallServerInterceptor.kt:63)
      okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
  #20995 "kafka-listener-19346" virtual
      okhttp3.internal.http2.Http2Writer.data(Http2Writer.kt:150)
      okhttp3.internal.http2.Http2Connection.writeData(Http2Connection.kt:332)
      okhttp3.internal.http2.Http2Stream$FramingSink.emitFrame(Http2Stream.kt:565)
      okhttp3.internal.http2.Http2Stream$FramingSink.close(Http2Stream.kt:612)
      okio.ForwardingSink.close(ForwardingSink.kt:37)
      okhttp3.internal.connection.Exchange$RequestBodySink.close(Exchange.kt:247)
      okio.RealBufferedSink.close(RealBufferedSink.kt:287)      
```

### 9.0.5 (1/10/2024 - 1/29/2024)

* json: update jackson to 2.16.1
  > refer to https://cowtowncoder.medium.com/jackson-2-16-rc1-overview-55dbb90c22d9
* mysql: updated and patched to 8.3.0
  > use "core.framework.mysql:mysql-connector-j:8.3.0-r2"
* db: support azure IAM auth
  > azure mysql flexible server supports IAM service account auth, to use access token instead of user/password
  > set db user to "iam/azure" to use azure iam auth
* search: update es to 8.12.0, switch es module repo to codelibs
  > !!! integration test breaks with JDK 21.0.2 (even with old version of es lib), refer to https://github.com/elastic/elasticsearch/pull/104347
  > !!! to run with JDK 21.0.2, workaround is to create EsExecutors.java and apply the fix locally
  > !!! add codelib maven repo to project

```kotlin
maven {
    url = uri("https://maven.codelibs.org/")
    content {
        includeGroup("org.codelibs.elasticsearch.module")
    }
}
```

### 9.0.4 (12/20/2023 - 1/9/2024)

* jre: published neowu/jre:21.0.1
* mysql: aggressively simplified mysql jdbc driver, removed unused features
  > add slow query support, decoupled core-ng and mysql classes
  > must use "core.framework.mysql:mysql-connector-j:8.2.0-r3"

### 9.0.3 (12/12/2023 - 12/19/2023)

* kafka: updated client to 3.6.1
* db: tweaked datetime related operations for virtual thread
  > use new date api if possible, mysql driver uses too many locks/sync for old Date/Timestamp impl
  > it recommends to map MySQL column type: LocalDate -> DATE, LocalDateTime -> DATETIME(6), ZonedDateTime -> TIMESTAMP(6)
  > with Timestamp, in mysql console, it is easier to use "SET @@session.time_zone" to adjust datetime value displayed
* mysql: updated mysql driver according to profiling result
  > use "core.framework.mysql:mysql-connector-j:8.2.0-r2"
  > simplified and tuned used code path

### 9.0.2 (12/7/2023 - 12/12/2023)

* stats: dump virtual threads on high cpu
* http: response "connection: keep-alive" header if client sends keep-alive header
  > to be compatible with http/1.0 client, like ab (apache benchmark) with "-k"
* mysql: patched mysql jdbc driver to support virtual thread and gcloud auth
  > use "core.framework.mysql:mysql-connector-j:8.2.0"
  > !!! for db-migration, pls continue to use "com.mysql:mysql-connector-j:8.2.0", as our patched version may remove unused features
  > refer to https://github.com/neowu/mysql-connector-j
  > refer to https://bugs.mysql.com/bug.php?id=110512

### 9.0.1 (12/01/2023 - 12/7/2023)

* thread: updated default virtual thread scheduler parallelism to at least 16
  > jdbc is not fully supported virtual thread yet, allow more virtual thread unfriendly tasks to run parallel
  > refer to https://bugs.mysql.com/bug.php?id=110512
* kafka: updated kafka listener to virtual thread, increased default concurrency to cpu * 16
  > now only 1 thread is pulling messages, and dispatched to {concurrency} threads
* thread: track virtual thread count
* http: use virtual thread to replace undertow worker pool

### 9.0.0 (09/01/2023 - 12/01/2023) !!! updated to Java 21

* kafka: updated client to 3.6.0
* search: update es to 8.11.1
* executor: removed executor config, provide builtin Executor binding, backed by virtual thread
  > virtual thread doesn't support currentThreadCPUTime, thus if in virtual thread, action.cpu_time won't be tracked
* sys: add "_sys/thread/virtual" diagnostic controller to print virtual thread dump
  > refer to https://openjdk.org/jeps/444 for more info about virtual thread
* mongo: update driver to 4.11.0
  > improved for virtual thread, https://www.mongodb.com/docs/drivers/java/sync/current/whats-new/#std-label-version-4.11
* mysql: update driver to 8.2.0

(pmd only support java 21 from 7.0, and 7.0/gradle pmd 7.0 support is not released yet, refer to https://github.com/gradle/gradle/issues/24502)

### 8.1.6 (07/24/2023 - 08/16/2023)

* mysql: update driver to 8.1.0
* kafka: updated client to 3.5.1
* search: update es to 8.9.0

### 8.1.5 (06/22/2023 - 07/10/2023)

* kafka: updated client to 3.5.0
* gradle: updated test dependency and kotlin DSL
  > prepare for gradle 9.0
* mongo: update driver to 4.10.1

### 8.1.4 (06/05/2023 - 06/13/2023)

* redis: supports SortedSet.increaseScoreBy()
* mongo: set connect timeout to 5s
* search: update es to 8.8.1
* search: set connect timeout to 5s
  > generally es is within same network, it doesn't need long connect timeout in order to fail fast
* kafka: updated client to 3.4.1
* http: updated okhttp to 4.11.0

### 8.1.3 (05/21/2023 - 06/04/2023)

* search: support es cloud
  > es host can be configured as full uri, e.g. http://es-0.es, or https://es-0.es:9200
  > added searchConfig.auth(apiKeyId, apiKeySecret)
* kibana: support es cloud
  > configure auth via: app.kibana.apiKey, sys.elasticsearch.apiKeyId, sys.elasticsearch.apiKeySecret
* api: support create api client with custom httpclient
  > removed api().httpClient(), pass custom built httpClient into api.client() instead

### 8.1.2 (05/08/2023 - 05/16/2023)

* search: fixed ElasticSearchLogInterceptor logging issue with chunked http entity
  > failed to generate trace log with bulkIndex
* search: fixed default max conn settings
* search: updated default timeout to 15s
  > tolerant more when es is busy
* db: update mysql driver to 8.0.33
* json: update jackson to 2.15.0

### 8.1.1 (03/14/2023 - 04/03/2023)

* ws: response corresponding close-code on connect according to exception
* httpClient: disabled okHTTP built-in followup and http-to-https redirect
  > those behavior should be impl on application level to have complete trace
* scheduler: trigger job via /_sys/job links refId and correlationIds
* search: update es to 8.7.0

### 8.1.0 (02/21/2023 - 03/09/2023)

* ws: update perf stats to track bytes read/write, similar like http client
* log: updated ws_active_channels, http_active_requests visualization
  > max -> split by host -> stacked
* ws: onMessage/onClose dispatched to task pool
* ws: websocket abnormal closure will trigger onClose event

### 8.0.13 (02/08/2023 - 02/21/2023)

* ws: add "ws_active_channels" stats
* kafka: update to 3.4.0
* ws: allow to provide custom rate limit config for ws connecting
  > use http().limitRate().add(WebSocketConfig.WS_OPEN_GROUP, ...)
* mongo: update driver to 4.9.0

### 8.0.12 (01/18/2023 - 02/07/2023)

* hash: removed hmac md5/sha1, hash.sha1 support
  > not used anymore
* monitor: added mongo monitor
* ws: add rate limit on ws connect
  > establish wss connection is expensive, especially won't be able to reuse http conn pool from LB
  > by default only allow to create 10 connections every 30s
* db: update mysql driver to 8.0.32
* kafka: update to 3.3.2

### 8.0.11 (01/10/2023 - 01/17/2023)

* mongo: update MongoMigration with 1 hour timeout
  > index creation on large collection could take long
* mongo: added runAdminCommand, and enhanced MongoMigration to support get properties easier
  > make it easier to determine env, and run different command
* mongo: not checking dns if mongodb+srv protocol
  > srv protocol is mainly used by mongo atlas, the readiness probe is mainly for self-hosting mongo in kube cluster
* mongo: added dropIndex() for migration
* search: update es to 8.6.0

### 8.0.10 (12/13/2022 - 01/04/2023)

* log: remove APP_LOG_FILTER_CONFIG from log-processor
  > not really useful in practice, log system should be designed to be capable of persisting massive data
* db: updated pool.maxIdleTime from 1 hour to 2 hours
  > with cloud auth, metadata service could be down during GKE control plane updating (zonal GKE only, not regional GKE)
  > increase maxIdleTime will hold db connection longer and improve efficiency of db pool, especially in cloud env, there is no firewall between app and db
* mongo: update driver to 4.8.1
* search: improved ForEach validation and clear scroll error handling
* http: use actual body length as stats.response_body_length

### 8.0.9 (11/09/2022 - 12/12/2022)

* json: update jackson to 2.14.0
* html: HTMLTemplate supports data uri
* http: tweaked websocket handling, support more close code
* log: finalize log-exporter design

### 8.0.8 (10/10/2022 - 11/09/2022)

* ext: update log-collector default sys.http.maxForwardedIPs to 2
  > to keep consistent with framework default values, use ENV to override if needed
* http: if x-forwarded-for header has more ip than maxForwardedIPs, put x-forwarded-for in action log context
  > for request passes thru http forward proxy, or x-forwarded-for spoofing, log complete chain for troubleshooting
* http: handle http request with empty path
  > browser or regular http client won't send empty path, only happens with raw http request sending by low level client
* http: update undertow to 2.3.0
* db: update mysql driver to 8.0.31
  > mysql maven group updated, it is 'com.mysql:mysql-connector-j:8.0.31'
* log: update slf4j api to 2.0.3
* log: added log-exporter to upload log to google storage
  > as archive, or import to big query for OLAP
  > currently only support gs://, support other clouds if needed in future
* search: update es to 8.5.0
  > es 8.5.0 has bug to break monitor, https://github.com/elastic/elasticsearch/issues/91259, fixed by 8.5.3

### 8.0.7 (09/23/2022 - 10/05/2022)

* ext: updated dockerfile for security compliance
  > in order to enable kube "securityContext.runAsNonRoot: true", docker image should use numeric user (UID)
* monitor: fixed kafka high disk alert message
  > kafka disk usage uses size as threshold, alert message should convert to percentage
* log-processor: support action log trace filter
  > to filter out trace to reduce storage, e.g. under massive scanning
* kafka: update to 3.3.1
  > kraft mode is production ready

### 8.0.6 (08/16/2022 - 09/23/2022)

* log-processor: always use bulk index, to simplify
  > ES already unified bulk index / single index thread pool, there is only one "write" pool
  > refer to https://www.elastic.co/guide/en/elasticsearch/reference/current/modules-threadpool.html
* http: update undertow to 2.2.19
* search: update es to 8.4.1
* db: support json column via @Column(json=true)
  > syntax sugar to simplify custom getter and setter to convert between String column and Bean
* kafka: update to 3.2.3
* mongo: json @Property should not be used on mongo entity class
  > same like db, view and entity should be separated
* db: mysql jdbc driver updated to 8.0.30

### 8.0.5 (07/28/2022 - 08/15/2022)

* mock: fixed MockRedis.list().range(), with negative start or stop
  > only impact unit test
* mongo: update driver to 4.7.1
* kafka: update to 3.2.1
* kafka: removed KafkaConfig.publish(messageClass) without topic support
  > it proved not useful to support dynamic topic, topic better be designed like table, prefer static naming / typing over dynamic
  > if really need something like request/replay, fan-in / fan-out pattern, still can be implemented in explicit way
* monitor: monitoring kafka topic/message type changes
* mongo: improve entity decoding
  > use switch to replace if on field matching

### 8.0.4 (07/10/2022 - 07/27/2022)

* log-collector: stricter request validation
* mongo: add connection pool stats metrics
* db: add experimental postgresql support
  > many new db products use postgres compatible driver, e.g. GCloud AlloyDB, CockroachDB
  > PostgreSQL lacks of many features we are using with MySQL, 1. affected rows, 2. QueryInterceptor to track no index used sql
* search: clear ForEach scroll once process done
* http: update undertow to 2.2.18
* search: update es to 8.3.2
* kafka: mark LONG_CONSUMER_DELAY as error if delay is longer than 15 mins
  > currently MAX_POLL_INTERVAL_MS_CONFIG = 30 mins, larger delay will trigger rebalance and message resending
  > system should be designed to process polling messages fast

### 8.0.3 (06/22/2022 - 07/08/2022)

* mongo: update driver to 4.6.0
* warning: removed @DBWarning, replaced with @IOWarning
  > e.g. @IOWarning(operation="db", maxOperations=2000, maxElapsedInMs=5000, maxReads=2000, maxTotalReads=10_000, maxTotalWrites=10_000)
* kafka: updated default long consumer delay threshold from 60s to 30s
* mongo: supports BigDecimal (map to mongo Decimal128 type)
* mongo: supports LocalDate (map to String)
* monitor: for critical errors, use 1 min as timespan (only send one alert message per min for same kind error)
  > in reality, ongoing critical error may flush slack channel, 1 min timespan is good enough to bring attention, and with lesser messages
* redis: supports SortedSet.remove()

### 8.0.2 (06/02/2022 - 06/17/2022) !!! @DBWarning is replaced with @IOWarning on 8.0.3, better upgrade to next version

* db: redesign db max operations, removed Database.maxOperations(), introduced @DBWarning
  > one service may mix multiple type of workload, some could be async and call db many times while others API needs to be sync and responsive
  > with new design, it allows to specify warning threshold in granular level
  > currently @DBWarning only support Controller/WS method, MessageHandler method, and executor task inherits from parent @DBWarning
* db: reduced default LONG_TRANSACTION threshold from 10s to 5s, log commit/rollback elapsed time

### 8.0.0 (05/19/2022 - 05/31/2022)  !!! breaking changes

* json: expose JSONMapper.builder() method to allow app create its own JSON parser
  > e.g. to parse external json with custom DeserializationFeature/MapperFeature
* search: add validation, document class must not have default values
  > with partialUpdate, it could override es with those default values
* search: update and partialUpdate return if target doc is updated
  > refer to https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html#_detect_noop_updates
* log-processor: action forward supports ignoring actions
* db: query.count() and query.projectOne() honor sort/skip/limit   !!! behavior changed !!!
  > repository.select() is designed to be syntax sugar, to make it easier to construct single table SQL (ORM is not design goal)
  > since it supports groupBy() projectOne() project(), now we removed all customized rules, it degraded to plain SQL builder
  > if you uses query.count(), it's better not set skip/limit/orderBy before that
  > with this, projection works as intended, e.g. query.limit(1); query.projectOne("col1, col2", View.class); results in "select col1, col2 from table limit 1"

### 7.10.8 (05/16/2022 - 05/19/2022)

* log-processor: action forward supports by result
  > e.g. only forward OK actions
* json: disabled ALLOW_COERCION_OF_SCALARS, to make it stricter
  > thru JSONMapper.disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
  > previously, [""] will convert [null] if target type is List<Integer>
* search: put detailed error in SearchException
* kafka: update to 3.2.0
* db: query.project() renamed to query.projectOne(), added query.project() to return List<View> !!!
  > should be easy to fix with compiler errors

### 7.10.7 (04/29/2022 - 05/12/2022)

* log-collector: make http().maxForwardedIPs() read property "sys.http.maxForwardedIPs"
  > so it can be configured by env
* stats: replace cpu usage with JDK built-in OperatingSystemMXBean.getProcessCpuLoad()
  > since jdk 17, java is aware of container, refer to https://developers.redhat.com/articles/2022/04/19/java-17-whats-new-openjdks-container-awareness
* rate-limit: update rate control config api to make it more intuitive
  > e.g. add("group", 10, 5, Duration.ofSeconds(1)) keeps 10 permits at most, fills 5 permits every second
  > e.g. add("group", 20, 10, Duration.ofMinutes(5)) keeps 20 permits at most, fills 10 permits every 5 minutes
* search: added retryOnConflict on UpdateRequest to handle
* search: added partialUpdate
* db: mysql jdbc driver updated to 8.0.29 !!! framework depends on mysql api, must use matched jdbc driver
* search: update es to 8.2.0

### 7.10.6 (04/19/2022 - 04/28/2022)

* db: tweak gcloud iam auth provider expiration time, make CancelQueryTaskImpl be aware of gcloud auth provider
* db: tweak query timeout handling
  > increase db socket timeout, to make MySQL cancel timer (CancelQueryTaskImpl) has more room to kill query
* http: update undertow to 2.2.17
  > UNDERTOW-2041 Error when X-forwarded-For header contains ipv6 address with leading zeroes
  > UNDERTOW-2022 FixedLengthStreamSourceConduit must overwrite resumeReads
* monitor: collect ES cpu usage
  > added highCPUUsageThreshold in monitor es config
* httpClient: verify hostname with trusting specific cert !!! behavior changed, check carefully
  > previously, HTTPClient.builder().trust(CERT) will use empty hostname verifier
  > now, only trustAll() will bypass hostname verifying, so right now if you trust specific cert, make sure you have CN/ALT_NAME matches the dns name (wildcard domain name also works)
* httpClient: retry aware of maxProcessTime
  > for short circuit, e.g. heavy load request causes remote service busy, and client timeout triggers more retry requests, to amplify the load
* es: support refresh on bulk request
  > to specify whether auto refresh after bulk request
* es: added timeout in search/complete request
* es: added DeleteByQuery
* es: added perf trace for elasticSearch.refreshIndex
* monitor: treat high disk usage as error
  > disk full requires immediate attention to expand
* action: use id as correlationId for root action
  > to make kibana search easier, e.g. search by correlationId will list all actions
  > log-processor action diagram also simplified

### 7.10.5 (04/01/2022 - 04/14/2022)

* search: truncate es request log
  > bulk index request body can be huge, to reduce heap usage
* db: support gcloud IAM auth
  > gcloud mysql supported IAM service account auth, to use access token instead of user/password
  > set db user to "iam/gcloud" to use gcloud iam auth
* monitor: add retry on kube client
  > to reduce failures when kube cluster upgrades by cloud
* http: removed http().httpPort() and http().httpsPort(), replaced with http().listenHTTP(host) and http().listenHTTPS(host)
  > replaced sys.http.port, sys.https.port with sys.http.listen and sys.https.listen
  > host are in "host:port" format, e.g. 127.0.0.1:8080 or 8080 (equivalent to 0.0.0.0:8080)
  > with cloud env, all envs have same dns/service name, so to simplify properties config, it's actually better to setup dns name to minic cloud in local
  > e.g. set "customer-service" to 127.0.0.2 in local dns, and hardcode "https://customer-service" as customerServiceURL
* log-collector: refer to above, use sys.http.listen and sys.https.listen env if needed
* kafka: redesigned /_sys/ controller, now they are /_sys/kafka/topic/:topic/key/:key/publish and /_sys/kafka/topic/:topic/key/:key/handle
  > publish is to publish message to kafka, visible to all consumers
  > handle is to handle the message on the current pod, not visible to kafka, just call handler to process the message (manual recovery or replay message)

### 7.10.4 (03/15/2022 - 03/31/2022)

* maven: deleted old published version older than 7.9.0
* redis: replaced ZRANGEBYSCORE with ZRANGE, requires redis 6.2 !!!
* redis: for list.pop always use "LPOP count" to simplify, requires redis 6.2 !!!
* redis: added RedisSortedSet.popMin
* redis: improved RedisSortedSet.popByScore concurrency handling
* kafka: collect producer kafka_request_size_max, collect kafka_max_message_size
  > stats.kafka_request_size_max is from kafka producer metrics, which is compressed size (include key/value/header/protocol),
  > broker size config "message.max.bytes" should be larger than this
  > action.stats.kafka_max_message_size is uncompressed size of value bytes, (kafka().maxRequestSize() should be larger than this)
* log-processor: added stat-kafka_max_message_size vis, added to kafka dashboard
* log-processor: updated stat-kafka_request_size vis, added max request size
* log: limit max size of actionLog.context() to 5000 for one key
  > warn with "CONTEXT_TOO_LARGE" if too many context values
* log: tweaked action log/trace truncation
  > increased max request size to 2M
  > check final action log json bytes before sending to log-kafka, if it's more than 2M, print log to console, and truncate context/trace
  > with snappy compression, it's generally ok with broker message.max.bytes=1M
  > in worst case, we can set log-kafka message.max.bytes=2M
  > will review the current setup and potentially adjust in future

### 7.10.3 (02/25/2022 - 03/14/2022)

* db: fix: revert previous update UNEXPECTED_UPDATE_RESULT warning if updated row is 0
* executor: log task class in trace to make it easier to find the corresponding code of executor task
* log-processor: update action-* context dynamic index template to support dot in context key
  > it's not recommended putting dot in action log context key
* log-processor: kibana json updated for kibana 8.1.0
  > TSVB metrics separate left axis bug fixed, so to revert GC diagrams back
* search: update es to 8.1.0
  > > BTW: ES cannot upgrade a node from version [7.14.0] directly to version [8.1.0], upgrade to version [7.17.0] first.
* mongo: updated driver to 4.5.0
  > removed mapReduce in favor of aggregate, it's deprecated by mongo 5.0  
  > refer to https://docs.mongodb.com/manual/reference/command/mapReduce/#mapreduce

### 7.10.2 (02/11/2022 - 02/23/2022)

* scheduler: replaced jobExecutor with unlimited cached thread pool
  > no impact with regular cases, normally scheduler-service in one application should only send kafka message
  > this change is mainly to simplify test service or non-global jobs (e.g. no need to put real logic to Executors in Job)
* jre: published neowu/jre:17.0.2
* search: update es to 8.0.0
  > Elastic dropped more modules from this version, now we have to include transport-netty4, mapper-extras libs
  > and it doesn't provide standard way of integration test, refer to https://github.com/elastic/elasticsearch/issues/55258
  > opensearch is doing opposite, https://mvnrepository.com/artifact/org.opensearch.plugin
* db: updated batchInsertIgnore, batchUpsert, batchDelete return value from boolean[] to boolean
  > this is drawback of MySQL thin driver, though expected behavior
  > with batch insert ignore (or insert on duplicate key), MySQL thin driver fills entire affectedRows array with same value, java.sql.Statement.SUCCESS_NO_INFO if updated count > 0
  > refer to com.mysql.cj.jdbc.ClientPreparedStatement.executeBatchedInserts Line 758
  > if you need to know result for each entity, you have to use single operation one by one (Transaction may help performance a bit)
* db: mysql jdbc driver updated to 8.0.28
  > one bug fixed: After calling Statement.setQueryTimeout(), when a query timeout was reached, a connection to the server was established to terminate the query,
  > but the connection remained open afterward. With this fix, the new connection is closed after the query termination. (Bug #31189960, Bug #99260)
* known bugs:
  > db, due to use affected rows (not found rows), if repository updates entity without any change, it warns with UNEXPECTED_UPDATE_RESULT, please ignore this, will fix in next version

