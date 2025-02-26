## Change log

### 9.1.7 (2/26/2025 - )

* sse: send ErrorResponse to client via "event: error" on exception

### 9.1.6 (2/10/2025 - 2/25/2025)

* http_client: tweak sse checking
* undertow: updated to 2.3.18.Final
  > due to vulnerability of old versions, has to update to latest despite potential memory consumption is higher
* sse: support method PUT/POST with body
* sse: channel.close() now closes sse connection gracefully
* sse: change retry instruction to 5s
  > in cloud env, backend timeout is set to 600s
* sse: channel.send() returns if sent successfully

### 9.1.5 (11/11/2024 - 01/22/2025)

* log-exporter: change gsutil to "gcloud storage"
* log-exporter: remove trace exporting
  > trace is only for troubleshooting, unnecessary for long term
* mongo: update driver to 5.2.1

> gsutil is deprecated and requires old version of python

### 9.1.4 (10/22/2024 - 11/08/2024)

* http_server: always write multipart uploading for temp file
* http_client: use 1 hour TTL for fallback dns cache
  > report error for permanent DNS issue after 1 hour
* jre: published neowu/jre:21.0.5
* log-exporter: replace "rm -rf" with "find -delete"
  > "find" is more memory efficient, "rm -rf" may cause OOM with deep dir and many files
* undertow: rolled back to 2.3.10
  > though 2.3.18 fixed memory leak, the memory consumption is much worse under load test (heap usage and GC)
* kafka: update to 3.9.0
  > changed compression from SNAPPY to ZSTD
* validation: @Pattern supports @Pattern(ignoreCase = true)
* search: make core.framework.search.ElasticSearch.deleteIndex idempotent (ignore if index is missing/deleted)
* redis: support expiring specific hash field
  > supported by redis 7.4

### 9.1.2 (8/9/2024 - 9/26/2024)

* search: loading from json into search request
  > for complex aggregation, refer to ElasticSearchAggregationIntegrationTest.java for usage
* log-processor: updated d3-graphviz, supports "includes" in `/diagram/arch`
* undertow: update to 2.3.17
  > io.undertow.server.DefaultByteBufferPool.threadLocalCache memory leak issue is fixed,
  > though i think it's worse than native ThreadLocal impl (with one thread per task model), now it's using Collections.synchronizedMap(new WeakHashMap<>())
* search: update es to 8.15.0
* tweak search query builder !!! breaking API change, just fix compiler error
  > to construct SearchRequest.query, use one of following
  > core.framework.search.query.Queries
  > co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders

### 9.1.1 (7/11/2024 - 8/7/2024)

* ws/sse: updated max process time
* kafka: update to 3.8.0
* http_client: preliminary sse support
  > example usage:

```
var request = new HTTPRequest(HTTPMethod.GET, "https://localhost:8443/sse");
try (EventSource source = client.sse(request)) {
    for (EventSource.Event event : source) {
        System.out.println(event.id() + " " + event.data());
    }
}
```

* monitor: fix stats type for mongo 7
* uuid: added uuid v7, can be used as db friendly primary key

### 9.1.0 (6/12/2024 - 7/9/2024)

* jre: published neowu/jre:21.0.3
* mysql: updated and patched to 8.4.0-r4
  > use "core.framework.mysql:mysql-connector-j:8.4.0-r4"
  > fixed native_auth_plugin, was configured as clear_text_plugin (not impact cloud env)
* sse: support server sent event
* ws: API changed !!! check all compilation errors
  > renamed all "room" to "group"
  > change WebSocketContext to WebSocketContext<T>, to support multiple websocket endpoints, be consistent with sse
* db: add azure IAM auth support
  > update db user to "iam/azure/username" format
* kafka: update to 3.7.1
* monitor: refresh kube client auth token every 10 mins

### 9.0.10 (4/29/2024 - 6/7/2024)

* mysql: updated and patched to 8.4.0-r2
  > use "core.framework.mysql:mysql-connector-j:8.4.0-r2"
* kafka: disabled client metric push
* kafka: respect @IOWarnings in /topic/:topic/key/:key/handle KafkaController
* search: update es to 8.14.0
* mongo: update driver to 5.10.0

### 9.0.9 (3/20/2024 - 4/18/2024)

* mysql: updated and patched to 8.3.0, fixed CJException should be wrapped as SQLException
  > make sure use "core.framework.mysql:mysql-connector-j:8.3.0-r2"
* thread: replace all synchronized with ReentrantLock/Condition, including RateControl/ShutdownHandling/Test
* http: convert some http error as warning
  > undertow "UT000133: Request did not contain an Upgrade header, upgrade is not permitted"
  > "response was sent, discard the current http transaction"
* search: update es to 8.13.2
* mongo: update mongo driver to 5.0.1

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

