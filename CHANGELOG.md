## Change log

### 7.6.13 (03/01/2021 - )

* mongodb: update driver to 4.2.2
  > According to official docs, this is a minor version so there are not breaking changes.
* search: update es to 7.11.2
  > since 7.11, elasticsearch no longer publishes OSS version, refer to docker/es for latest es/kibana configures.
  > codelibs stopped to publish modules we use, so we publish them under core.framework.elasticsearch.module. (refer to https://mvnrepository.com/artifact/org.codelibs.elasticsearch.module/lang-painless)
  > pls update gradle maven repo setting as following
  ```groovy
    repositories {
        maven {
            url 'https://neowu.github.io/maven-repo/'
            content {
                includeGroupByRegex 'core\\.framework.*'
            }
        }
    }
  ```
* db: allow use multiply operator (not wildcard) in sql, e.g. select column*3 from table

### 7.6.12 (02/02/2021 - 03/01/2021)

* cache: for redis local cache, handle exception as warning if redis is not accessible
* http: parse content-type value case insensitively
  > refer to https://www.w3.org/Protocols/rfc1341/4_Content-Type.html
  > treat, "Application/json" same as "application/json"
* rate: fix concurrent requests might cause negative timeElapsed bug

### 7.6.11 (01/26/2021 - 02/01/2021)

* http: change max allowed url length from 1000 to 2000
  > Microsoft Azure AD oauth call back url may have long query param,
  > by considering browser limitation/search engine/CDN/etc, to use 2000 as default setting
* redis: add hyperLogLog support

### 7.6.10 (01/18/2021 - 01/25/2021)

* api: for max process time, webservice will consider both http client timeout and remaining process time of current action
* monitor: support multiple alert channels
* es: update to 7.10.2
* http: collect http server active requests to reflect how many active requests are in process (to analyze performance with cpu usage, worker thread pool size and etc)

### 7.6.9 (01/04/2021 - 01/15/2021)

* contentType: according to RFC and iana, application/json should not have charset, and use utf8 as default charset.
  > refer to https://tools.ietf.org/html/rfc7159#section-8.1, https://www.iana.org/assignments/media-types/application/json
* http: introduced HTTPConfig.maxProcessTime(), to specify client timeout or cloud lb backend timeout,
  > for default value, http uses 30s for initial request, kafka listener uses 30mins for whole polled batch.
  > maxProcessTime will be passed to subsequent sync action thru "timeout" http header, via WebServiceClient
  > http client retry will consider time left from max process time
  > action will check for slow process if took more than 80% max process time
* http: added HTTPConfig.maxEntitySize(), to specify max body size and multipart file upload size

### 7.6.8 (12/21/2020 - 12/30/2020)

* kafka: update to 2.7.0
  !!! kafka client causes high cpu issue with 2.5/2.6, refer to https://issues.apache.org/jira/browse/KAFKA-10134
* validation: added @Digits, constricts maximum number of integral/fractional digits
* log: SQLParams/HTTPHeader truncate long string param
* db: added Repository.batchInsertIgnore(entities)

### 7.6.7 (12/08/2020 - 12/17/2020)

* http: update undertow to 2.2.3
* httpClient: support fallback dns cache, to reduce intermittent dns resolving failures for external domains
* log-processor: support JOB_INDEX_OPEN_DAYS, JOB_INDEX_RETENTION_DAYS env to configure how long to keep log index open and when to delete
* redis: improve error handling by validating all input before sending data
* log: fixed kafka log appender not sending failed_to_start error, if exception was thrown by configure()
* db: validate params of batch operations must not be empty

### 7.6.4 (11/19/2020 - 12/07/2020)

* kafka: update snappy-java to 1.1.8.1, (according to release note, there is small performance improvement)
* kafka: added kafka().maxRequestSize() to configure max request size, this setting must be consistent with broker
* stat: track and warn on java process high vmRSS usage, for docker OOMKill cases better enable -XX:NativeMemoryTracking=summary with detailed usage report
* diagnostic: added /_sys/proc to report process info (mainly memory usage of java process) on linux
* http: improve error message on query/form/json bean parsing, return generic message with 400 for vulnerability scanning

### 7.6.3 (11/16/2020 - 11/19/2020)

* module: DBConfig.repository() returns repository object, to make it easier to extend, same applied to es search type and mongo collection
* http: tweak http request/response logging to gracefully handle vulnerability scan request from internet
* json: update jackson to 2.11.3 es lib still depends on jackson-dataformat 2.10.4, it can be safely ignored if not use smile/yml/cbor
* mysql: update driver to 8.0.22, disabled mysql connection cleanup thread,
  !!! must update mysql driver to 8.0.22 as framework uses type referring to set system property.

### 7.6.2 (10/30/2020 - 11/12/2020)

* redis: support zadd with multiple keys and rangeByScore
* monitor: improve kube pod monitor error message, to provide better error message if pod was killed in the middle
* es: update to 7.10.0

### 7.6.1 (10/15/2020 - 10/29/2020)

* http: httpClient does not read response body if status code is 204
* executor: improve warning for task rejection/cancellation during shutdown
* db: added Repository.insertIgnore(entity) to handle duplicated key / constraint violation cases without catching UncheckedSQLException
* redis: added initial zset support

### 7.6.0 (09/30/2020 - 10/13/2020) !!! only support java 15

* java: update to java 15, please make sure to upgrade build/jenkins and all docker runtime to 14 before upgrading framework
  !!! recommended to use adoptopenjdk/openjdk15:alpine-jre for docker image
* http: api client extends keep alive duration to 5 mins to improve internal api calls, track resolving dns and establishing connection performance
* http: set jdk networkaddress.cache.ttl/networkaddress.cache.negative.ttl to 300/0 by default
* http: track action.stats.http_retries for http client retries
* cache: renamed stats.cache_hit/miss to cache_hits/misses,
  !!! log processor kibana index updated accordingly.

### 7.5.12 (09/23/2020 - 09/29/2020)

* http: ForbiddenException supports errorCode, ipv4AccessControl throws forbiddenException with IP_ACCESS_DENIED error code
* es: update to 7.9.2
* search: add searchConfig.maxResultWindow() to specify max window size

### 7.5.11 (09/17/2020 - 09/21/2020)

* log: use CORE_APP_NAME instead of APP_NAME env var to override appName APP_NAME shadowed by gradle application plugin linux start script
* kafka: renamed stats.consumer_lag to stats.consumer_delay and updated kibana dashboard
  !!! please update to latest log-processor to refresh log es indices.
* executor: track task delay via stats.task_delay, indicator of whether thread pool is queuing
* executor: log task actionId in parent trace to improve visibility
* http: update undertow to 2.2.0

### 7.5.10 (09/07/2020 - 09/16/2020)

* log: moved action.cpu_time to action.stats.cpu_time
  !!! please update to latest log-processor to refresh log es indices.
* log: added action.stats.http_content_length to track http body size
* httpClient: update okHTTP to 4.9.0
* executor: improved future.get(), report both action and id on exception, to help troubleshooting

### 7.5.8 (09/03/2020 - 09/07/2020)

* api: WebServiceClientInterceptor will process onResponse before validateResponse, let interceptor see error http response as well
* es: update to 7.9.1
* inject: check if any bean has @Inject field but not autowired e.g. someone may pass a manually created object into api().service(), but later someone else may add @Inject field in serviceImpl and failed to realize bind() was not used

### 7.5.7 (08/30/2020 - 09/03/2020)

* json: disable to deserialize ordinal number to enum (the default setting of jackson is not strict)
* api: use generic error message when webservice client or controller failed to deserialize json
  !!! not to leak too much internal info to public, e.g. jackson return all possible enums in error message.
* httpClient: HTTPClient.builder().trust(CERT) to add system trusted issuers as well
* stats: collect cpu usage after the startup, to ignore high cpu usage during startup

### 7.5.5 (08/24/2020 - 08/30/2020)

* stats: updated cpu usage calculation to support docker (overhead improved compare to previous impl which used threading time)
  !!! due to within java, there is no way to get exact cpu shares, in kube env, better to put cpu limit as integer, like 1000m or 2000m
  !!! to keep it simple, retrieve available cpu count by java API (Runtime.getAvailableProcessors()), and not going hard way e.g. read "/sys/fs/cgroup/cpu/cpu.shares".
* stats: collect thread dump via stats.info if cpu usage is high
* httpClient: support request level connectTimeout and timeout (read/write)
* log: unified BytesLogParam and JSONLogParam, to make cache returned log maskable

### 7.5.4 (07/31/2020 - 08/24/2020)

* kafka: rollback kafka java client to 2.4.1
  !!! https://issues.apache.org/jira/browse/KAFKA-10134 is not fully fixed.
* es: update to 7.9.0
* monitor: support more flexible notification config, allow notifying certain slack channel based on matching criteria
  !!! pls update monitor config similar like

```json
{
  "ignoreErrors": [
    {"apps": ["website"], "errorCodes": ["PATH_NOT_FOUND"], "severity": "WARN"}
  ],
  "criticalErrors": [
    {"errorCodes": ["FAILED_TO_START", "POD_FAILURE"]}
  ],
  "kibanaURL": "http://kibana:5601",
  "channels": {
    "actionWarnChannel": {"severity": "WARN", "indices": ["trace", "stat"]},
    "actionErrorChannel": {"severity": "ERROR", "indices": ["trace", "stat"]},
    "eventWarnChannel": {"severity": "WARN", "indices": ["event"]},
    "eventErrorChannel": {"severity": "ERROR", "indices": ["event"]},
    "additionalErrorCodeChannel": {"apps": ["product-service"], "errorCodes": ["PRODUCT_ERROR"]}
  }
}
```    

* db: disallow single quote (') in sql, this is to enforce prepared statement, not allowing concat string values indirectly into dynamic sql
  !!! make sure to review all exiting usages, query.where(), database.select()/execute() etc,
  !!! this also disallows function usage, like IFNULL(column, 'default'), but in our design, we prefer to simplify in first place, by either saving exact needed data, or handle in java code
* db: query.where(condition) enclose the condition with parentheses if it has OR operator
* monitor: added es_gc stats for es monitor
* monitor: added kafka monitor (heap/gc/network), and kibana visualizations
* stats: tweak kibana field format, update kibana bytes fields with better formatting
* stats: simplify stats key for jvm_gc and es_gc update consumer_lag_in_ms to nanoseconds                  
  !!! pls use same version of monitor/log-processor and framework.
* test: added mockito builtin MockitoExtension via mockito-junit-jupiter, removed framework one
  !!! old org.mockito.MockitoAnnotations.initMocks(this) is deprecated, use @ExtendWith(MockitoExtension.class)
* redis: update redis config to support different port other than 6379 potentially to use Envoy as redis proxy, by considering the future of service mesh and isito development refer to https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/other_protocols/redis#arch-overview-redis
  it does not support SCAN/PUBSUB command yet, so the usage is kind of limited. e.g. partition redis db, or remote only cache other options are like managed redis service, e.g. gcloud memorystore the official one moves slowly, https://github.com/RedisLabs/redis-cluster-proxy
* cache: cache config api changed
  !!! replace cache().local() cache().remote() back to cache().add()
  !!! for multiple level caching, use cache().add().local()

### 7.5.1 (07/23/2020 - 07/31/2020)

* utils: add Sets.newEnumSet and Maps.newEnumMap shortcuts
* httpClient: remove HTTPClientBuilder.tlsVersions(), jdk 14.0.2 fixed TLSv1.3 issues refer to https://bugs.openjdk.java.net/browse/JDK-8236039, https://github.com/golang/go/issues/35722
* ide: added javax.annotation.Nullable to improve IDE/Intellij nullability analysis, no runtime impaction.
* ws: update channel interface to be generic typing, only support bean message for client/server
* es: update to 7.8.1

### 7.4.14 (07/13/2020 - 07/23/2020)

* redis: change expiration precision from second to milliseconds
* shutdown: give resources at least 1s to terminate gracefully
* log: add info context to stat document, for collecting additional info, mainly for monitor (pls update log-processor/monitor to latest)

### 7.4.13 (07/09/2020 - 07/13/2020)

* cache: optimize local cache performance
* cache: add cache tracking, includes stats.cache_size, stats.cache_hit and stats.cache_miss
* log-collector: add validation for request, max error message size to 1000, max action size to 200, max error code size to 200

### 7.4.12 (06/29/2020 - 07/09/2020)

* cache: support 2 level cache
  !!! with redis configured, cache().local() will be using local + redis 2 levels cache, and using redis channel to notify invalidated keys
  !!! cache().local() should be used in performance sensitive area with high hit rate, to reduce number of redis commands overhead (but to increase JVM GC burden)
  !!! cache().remote() keeps old behavior.
* httpClient: disallowed new HTTPClientBuilder(), use HTTPClient.builder()

### 7.4.11 (06/19/2020 - 06/29/2020)

* es: update to 7.8.0
* ws: reduce warnings on sending message to closed channel (occurs on sending message before channel close listener finish)
* kafka: kafka producer/consumer will be created after startup and wait until kafka uri is resolvable in kube env, this improves recovery time if both kafka and app recreated at same time, and this make kafka has same behavior of other db (mysql/redis/etc)

### 7.4.10 (06/10/2020 - 06/18/2020)

* monitor: tweak kube monitor pod not ready error message
* stats: track jvm_non_heap_used by default
* cache: gracefully handle incompatible json structure from redis, to avoid clearing cache keys manually
* inject: remove method injection support
  !!! from the actual usage, only method injection use case is to do init with dependencies we don't want to put in fields, but generally we prefer calling init method explicitly in modules to make review easier.

### 7.4.9 (06/04/2020 - 06/09/2020)

* http: for static content controllers, convert client abort exception to warning (still better use CDN/Storage solution to serve static resources)
* log-collector: ignore sendBeacon request with empty body, (browser/privacy/plugin may impact sendBeacon's behavior)
* ws: support limitRate for websocket message listener
* ws: return appropriated close code according to exception type
* es: update to 7.7.1

### 7.4.8 (05/26/2020 - 06/02/2020)

* monitor: fix kube monitor false alert with too many restarts
* shutdown: support env SHUTDOWN_DELAY_IN_SEC / SHUTDOWN_TIMEOUT_IN_SEC to fine control graceful shutdown in kube env (check source to see details)

### 7.4.7 (05/25/2020 - 05/25/2020)

* db: rename suppress flag method to Database.suppressSlowSQLWarning
* monitor: use app/action/errorCode as group, show one group every 4 hours

### 7.4.6 (5/22/2020 - 5/25/2020)

* config: moved site().publishAPI() to api().publishAPI(), sys property key ("sys.publishAPI.allowCIDR") remains same
* site: if called site(), then User-Agent and Referrer will be logged in action log context
* kafka: log key in action context for bulk message handler
* db: warning for sql queries not using index or using bad index, for expected queries, use following to disable Database.suppressSlowSQLWarning(true)
  database.select(...)
  Database.suppressSlowSQLWarning(false)
  !!! requires mysql jdbc driver 8.0+, recommend to use same or higher version referred by the framework.
* monitor: fixed kube 1.16 TLSv1.3 issues (workaround to downgrade to TLSv1.2, and wait newer version of JDK)
  refer to https://bugs.openjdk.java.net/browse/JDK-8236039, https://github.com/golang/go/issues/35722

### 7.4.5 (5/19/2020 - 5/22/2020)

* httpClient: update okHTTP to 4.7.2
* mongo: for unit test, start test mongo server on random port, to avoid multiple tests running on same server

### 7.4.4 (5/13/2020 - 5/18/2020)

* validation: removed @Length, replaced with @Size (now @Size works with string/list/map), to simplify and follow style of javax.validation.constraints
  !!! replace all @Length with @Size
* es: update to 7.7.0
* kafka: added uri parser, to support kafka uri without port (now it doesn't require port in url in all db/kafka/redis/mongo/es)
* httpClient: update okHTTP to 4.7.0

### 7.4.3 (5/8/2020 - 5/13/2020)

* httpClient: simplify form post masking handling and log uri instead of complete requestURI
* validator: make default message more user friendly

### 7.4.2 (4/27/2020 - 5/8/2020)

* mongo: update driver to 4.0.2
* httpClient: support to trust specific https cert
* http: removed undertow HTTP metrics undertow is using jboss threading and EnhancedQueueExecutor, there is overhead to collect stats with jmx, and they are not critical stats
* redis: start preparing for Redis6/RESP3 (later version may requires redis 6)
* httpClient: update okHTTP to 4.6.0
* httpClient: mask form body param
* monitor: monitor failed pod in kube namespaces
* monitor: support monitor es cluster
  !!! MonitorConfig.es.hosts (list) changes to es.host, only need to connect to one of cluster node to get all nodes info
* es: support cluster to have multiple hosts in SearchConfig.host(host) as comma separated

### 7.4.1.1 (4/24/2020)

* http: improve x-forwarded-for parser to support rfc7239, as Azure AG will use ipv4:port format in x-forwarded-for header
* kafka: rollback kafka client to 2.4.1, java-kafka 2.5.0 client driver causes cpu 100% during startup/rebalance if kafka server is not available or slow
  !!! due to behavior change of org.apache.kafka.clients.consumer.KafkaConsumer.pull -> updateAssignmentMetadataIfNeeded

### 7.4.1 (4/16/2020 - 4/23/2020)

* kafka: update to 2.5.0
* kafka: removed maxRequestSize config, as 1M is good enough for our projects, and https://issues.apache.org/jira/browse/KAFKA-4203 is fixed on 2.5.0
* db: add characterEncoding=utf-8 to driver properties by default, can be override by jdbc url
* http: update undertow to 2.1.0

### 7.4.0 (4/1/2020 - 4/14/2020) !!! only support java 14

* java: update to java 14, please make sure to upgrade build/jenkins and all docker runtime to 14 before upgrading framework
* http: updated built-in https self sign cert if you encounter chrome error for accessing localhost, open chrome://flags/#allow-insecure-localhost to enable this flag
* crypto: removed AES/RSA/Signature support cryptography is hard to get right and algorithm/best practice changes over time, for cloud env, always to use KMS (gcloud/aws/azure)
  the default impl is not considered as best practice, so we removed it from the framework there is no way to encapsulate simple/non-intrusive API to cover all cases, e.g. with CHACHA20-POLY1305 or AES-GCM, it requires managing and sharing nonce/IV, like either put IV as part of cipher text, or
  share in different channel, and IV should be different every time with RSA or X25519 it still needs to encrypt a symmetric key to encrypt final content
* es: update to 7.6.2
* db: added max operations per action check (default is 5000), to protect bad impl (e.g. infinite loop with db calls) or bad practice (make action too long for zero-down time release)
* db: added errorType (INTEGRITY_CONSTRAINT_VIOLATION) in UncheckedSQLException, to support catch duplicate key exception in business logic
* httpClient: update okHTTP to 4.5.0
* http: !!! start http server on https:8443 by default if not specifying sys.http.port and sys.https.port currently all services use https/h2, make sure you set sys.http.port to start http listener if needed, e.g. with Azure AG -> http/8080 website since AG doesn't support self-signed cert make sure
  to update health-check of the latest monitor/log-processor to HTTPS:8443

### 7.3.12.1 (3/25/2020 - 4/1/2020)

* db: added query.in(field, params) shortcut to build dynamic "where in clause"
* log-collector: added param app.cookies to collect specific cookies from request. (this is due to safari expires localStorage and cookies write by js, only way to long term track is thru first party cookie)

### 7.3.11 (3/23/2020 - 3/25/2020)

* kafka: set linger.ms to 5ms by default to improve batching under load
* security: add X-Frame-Options in WebSecurityInterceptor

### 7.3.10 (3/12/2020 - 3/19/2020)

* log: increase max length of error message to 1000, to show more info if needed in notification
* log-collector: app.allowedOrigins checks root domain (endsWith) to simplify config, e.g. app.allowedOrigins=example.com allows https://api.example.com
* db: removed db().encryptedPassword() config, as it recommend to use cloud provider such as KMS to encrypt/decrypt
* kafka: update to 2.4.1

### 7.3.9 (3/8/2020 - 3/11/2020)

* es: update to 7.6.1
* httpClient: update okHTTP to 4.4.1
* executor: support submitting task without parent action, for more flexible process flow with 3rd party lib, e.g. gcloud pub/sub or other long running process outside framework

### 7.3.8 (2/19/2020 - 3/3/2020)

* session: add domain (request.domain or cookieSpec.domain if specified) to session redis key, to prevent hijack sessionId from different domain if shared session redis
* bean: report error if @Inject is used on static fields/methods which is unintended usage

### 7.3.7 (2/18/2020 - 2/19/2020)

* api: when mark @Deprecated in API method, it won't show in /_sys/api and log warn if called
* monitor: add action in alert message
* httpClient: update okHTTP to 4.4.0

### 7.3.6 (2/7/2020 - 2/17/2020)

* monitor: add host in alert message for stats, change message format to make it easier to copy/paste to kibana search box
* db: not allow put @NotNull on @PrimaryKey, and validate assigned id must not be null on inserting
* es: update to 7.6.0
* mongo: update driver to 3.12.1

### 7.3.5 (1/30/2020 - 2/6/2020)

* httpClient: support retry on 429 too many requests
* monitor: added es monitoring, refer to https://github.com/neowu/core-ng-project/wiki/Ext for example config
* log-processor: added realted es visualization and dashboard

### 7.3.4 (1/29/2020 - 1/30/2020)

* es: update to 7.5.2
* redis: added info
* monitor: added redis monitoring, refer to https://github.com/neowu/core-ng-project/wiki/Ext for example config

### 7.3.3 (1/22/2020 - 1/23/2020)

* log: replaced "server_ip" with "host" field in action/stat log
* stat: warn on high cpu usage and heap usage

### 7.3.2 (1/17/2020 - 1/22/2020)

* db: change query.count() to return long instead of int
* monitor: added monitor to alert for action/event (will add more features like redis/es check, sanity/deep health check)
* log: replace "server_ip" with "host" field in action/stat log, (only on log-processor as first step), more friendly in kube env added "result" / "error_code" / "error_message" to stat log, for future monitoring support

### 7.3.1 (12/20/2019 - 1/15/2020)

* es: update to 7.5.1
* kafka: use null as key if not specified, since from kafka 2.4.0 it supports sticky partitioning, refer to refer to org.apache.kafka.clients.producer.internals.DefaultPartitioner set producer max request size to match broker default setting
* log-collector: validate event.info size with max=900k

### 7.3.0 (12/11/2019 - 12/18/2019)

* kafka: update to 2.4.0
* cache: support use both local and remote cache, local cache is for rarely changed but frequently accessed data
  !!! cache().add() renamed to cache().remote()
  !!! by default local cache uses 10% max heap
  !!! carefully use local cache on prod as it may not refreshed in time, use message to notify if worth

### 7.2.2 (12/02/2019 - 12/09/2019)

* es: update to 7.5.0
* httpClient: fixed CookieManager not deleting removed cookie from response

### 7.2.1 (11/20/2019 - 12/02/2019)

* scheduler: change Job interface to execute(JobContext context), to provide scheduledTime to job,
  !!! since it's easy migration, framework doesn't make it backward compatible (e.g. support old/new methods in job interface), pls just change all job classes)
* log-collector: update default to https:8443, gcloud lb accepts self-sign cert, so to use h2 by default, for other cloud/env, use kube env SYS_HTTP_PORT: 8080 to define http listener if needed
* json: update jackson to 2.10.1, refer to https://medium.com/@cowtowncoder/jackson-2-10-features-cd880674d8a2

### 7.2.0 (11/17/2019 - 11/20/2019)  !!! only support java 13, update docker/build server first before migration !!!

* project: updated source/target to java 13
* kafka: warn when consumer lag is too long, default is 60s
* ws: support wsContext.all() to return all channels set default text message max size to 10M support register listener with clientMessageClass and serverMessageClass (type safety, one ws path only allow one client message class and one server message class)
* api: updated WebServiceClientInterceptor to support both onRequest/onResponse

### 7.1.8 (11/8/2019 - 11/16/2019)

* http: convert "UT000128: Remote peer closed connection before all data could be read" exception to warn with errorCode=FAILED_TO_READ_HTTP_REQUEST this could be happen in event collector, the browser/app may be terminated before finish sending events, which causes UT000128
* shutdown: allow use env SHUTDOWN_TIMEOUT_IN_SEC to define shutdown timeout corresponding to kube terminationGracePeriodSeconds, (default is still 25s)
* http: request.requestURL() will be x-forwarded-host aware (for proxy with different domain in front of site, e.g. Azure AppGateway with different domain)
* log: updated actionLog context/stats/perf_stats names to snake cases for consistency also refer to https://www.elastic.co/guide/en/beats/devguide/current/event-conventions.html
  update log-processor will update kibana config automatically
* kafka: added actionLog stat.consumer_lag_in_ms to record lag between producer and consumer

### 7.1.7 (11/4/2019 - 11/6/2019)

* es: update to 7.4.2
* log-collector: allow client to use navigator.sendBeacon() with content-type=text/plain to bypass CORS check (due to navigator.sendBeacon doesn't preflight)

### 7.1.6 (10/24/2019 - 11/3/2019)

* kafka: update to 2.3.1
* es: update to 7.4.1
* search: update ElasticSearch.createIndex to ElasticSearch.putIndex, to support create index or update mappings, for es migration

### 7.1.5 (10/21/2019 - 10/23/2019)

* log-collector: supports stats to collect performance data
* session: added SessionContext.invalidate(key, value) to support kick out specific login user

### 7.1.4.1 (10/17/2019 - 10/21/2019)

* action: added app:start/app:stop action to track app container life cycle (e.g. killed/recreated by kube accidentally)
* api: exposed api().createClient() to allow create multiple instance of api client with different service url
* httpClient: tweak retry condition for connect timeout
* log-collector: allow post for navigator.sendBeacon()
* log-collector: index event time as @timestamp, use received_time for received time on server side

### 7.1.3 (10/16/2019)

* http: remove forcing TLSv1.2 as latest JDK already fixed TLSv1.3 issue
* http: update XNIO to 3.7.6, monitor work queue size and busy thread size, for visibility under heavy load

### 7.1.2 (10/8/2019 - 10/15/2019)

* httpClient: update okHTTP to 4.2.2
* db: to check connection validity before using, e.g. when gcloud SQL does maintenance, connections will be closed on server side
* websocket: support onClose message handling, and track open/close in action log
* websocket: support send bean, register by http().bean()

### 7.1.1 (10/7/2019 - 10/8/2019)

* log-processor: set log-es default to 1 shard and 10s refresh interval
* search: add keep alive setting in es high level java client
* http: support X_FORWARDED_HOST header for external LB (Azure App Gateway)

### 7.1.0 (10/1/2019 - 10/7/2019) !!! Action Log format changed, must update both core-ng and log-processor to same version !!!

### action-log will be send to new topic (action-log-v2), to avoid error during transition, only impact could be some old action-log is not indexed which is minor

* log: support put multiple values in same action log context, to make bulk handler / api easier to track, ActionLogContext.get(key) returns List<String> now
* log-collector: add errorMessage field in event, to keep consistent with action log
* httpClient: update okHTTP to 4.2.0
* search: update es to 7.4.0

### 7.0.3 (9/4/2019 - 9/19/2019)

* log-processor: log-processor will import kibana objects during start, set env KIBANA_URL and KIBANA_BANNER in kube env to enable only support kibana 7.3.1+
* search: support track total hits

### 7.0.2 (9/3/2019 - 9/4/2019)

* httpClient: added callTimeout as last timeout defense, as in prod env we encountered http client handing with timeout set java.lang.Thread.State: RUNNABLE at java.net.SocketInputStream.socketRead0(java.base@12.0.2/Native Method)
  at java.net.SocketInputStream.socketRead(java.base@12.0.2/Unknown Source)
  at java.net.SocketInputStream.read(java.base@12.0.2/Unknown Source)
  at java.net.SocketInputStream.read(java.base@12.0.2/Unknown Source)
  at okio.InputStreamSource.read(Okio.kt:102)
  at okio.AsyncTimeout$source$1.read(AsyncTimeout.kt:159)
  at okio.RealBufferedSource.indexOf(RealBufferedSource.kt:349)
  at okio.RealBufferedSource.readUtf8LineStrict(RealBufferedSource.kt:222)
  at okhttp3.internal.http1.Http1ExchangeCodec.readHeaderLine(Http1ExchangeCodec.kt:210)
  at okhttp3.internal.http1.Http1ExchangeCodec.readResponseHeaders(Http1ExchangeCodec.kt:181)
  at okhttp3.internal.connection.Exchange.readResponseHeaders(Exchange.kt:105)
  at okhttp3.internal.http.CallServerInterceptor.intercept(CallServerInterceptor.kt:82)
  at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:112)
  at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:87)

### 7.0.1 (8/28/2019 - 8/30/2019)

* scheduler: support hourly trigger
* redis: support set().size()

### 7.0.0 (8/9/2019 - 8/27/2019) !!! only support jdk 12, recommended to update to 6.13.9 and all runtime to adoptopenjdk-12 before updating to 7.0.0 !!!

* project: updated source/target to java 12
* db: drop oracle support, only support mysql to simplify

### 6.13.9 (8/8/2019)  !!! this is the last version for JDK 11, update to this version then start update all JDK runtime to 12+ !!!

* config: update controller inspector to adapt to JDK 12+

### 6.13.8 (8/1/2019 - 8/6/2019)

* http: support allow/deny large ip ranges (only support ipv4 for now)
* search: update es to 7.3.0

### 6.13.6.1 (7/29/2019 - 8/1/2019)

* bean: support LocalTime type in JSON/QueryParam (Cache/ES/Kafka/API)
* property: module property overriding will check whether key is defined in property file first, to prevent env may have keys not in property file causes unexpected behavior of runtime

### 6.13.5 (7/24/2019 - 7/26/2019)

* http: clientIp parser validates ip in x-forwarded-for header
* http: when request contains invalid cookies char, log cookies header and return 400
* config: module.bind() will return overridden bean if any

### 6.13.4 (7/23/2019)

* redis: support set().pop()

### 6.13.3 (7/22/2019)

* mongo: support mongo migration
* lib: various lib update

### 6.13.2 (7/16/2019 - 7/19/2019)

* kafka: inject messagePublisher as mock in integration-test, to make it easier to verify

### 6.13.1 (7/4/2019 - 7/15/2019)

* db: add Query.groupBy
* http: load session as late as possible, so for sniffer/scan request with sessionId, it won't call redis every time even for 404/405

### 6.13.0 (6/26/2019 - 7/1/2019)

* search: update es to 7.2.0
* search: fixed: to support multiple es integration jobs run on same build server
* kafka: update to 2.3.0
* httpClient: update okHTTP to 4.0.0

### 6.12.8.1 (6/19/2019)

* httpClient: make charset parsing more robust, some charset values have a version attribute.

### 6.12.8 (6/12/2019 - 6/17/2019)

* log-collector: stripe on parsing app.allowedOrigins config
* log: add sessionHash in ActionLogContext to improve data analytic
* httpClient: make okHTTP use default ConnectionSpec to be able to connect to most servers

### 6.12.7 (5/23/2019 - 6/6/2019)

* search: update es to 7.1.1
* classpath/properties: check if there are multiple resources with same name in different jars (within same classapth)
* kafka: update to 2.2.1

### 6.12.6 (5/20/2019 - 5/21/2019)

* http: update okHTTP to 3.14.2
* search: (bug) ElasticSearchMigration throws exception on failure

### 6.12.5 (5/7/2019 - 5/13/2019)

* log-processor: support sys.log.appender to configure whether index log-processor action logs, by default it's empty (to configure by env in kube)
  console -> output to console elasticsearch -> index directly to elasticsearch
* search: update es to 7.0.1
* cache: throw exception if loader returns null when called cache.get(key, loader)
* site: not set csp by default, in case application may generate it dynamically, (and even with static setting, it's set by sys.properties explicitly anyway)

### 6.12.4 (4/17/2019 - 4/25/2019)

* mongo: fix: unit test to support multiple test mongo servers on different ports
* search: fix: elasticsearch local test conflicts with mongo java server
* json: explicitly defined ZonedDateTime/LocalDateTime format, to make it more strict to comply with ES and js, (put 3 digits for nano fraction)

### 6.12.3 (4/9/2019 - 4/17/2019)

* http: update okHTTP to 3.14.1
* search: update es to 7.0.0, change index.flush to index.refresh (those 2 are different in ES)
* sys: sys.properties supports allowCIDRs in 2 formats, 1) cidr,cidr 2) name1: cidr, cidr; name2: cidr

### 6.12.2 (4/2/2019 - 4/9/2019)

* http: update undertow to 2.0.20
* search: update es to 6.7.1
* test: added assertEnumClass(class1).hasAllConstantsOf(class2)
* http: update http().bean() to accept only one class (for simplification), support register enum
* http: revert okHTTP to 3.12.2 due to https://github.com/square/okhttp/issues/4875
* log: tweak trace log, still attempt to show warning if reached max trace length (to limit overall trace within 900k)

### 6.12.1 (4/1/2019)

* kafka: fix kafka producer metrics can be NaN

### 6.12.0 (3/18/2019 - 4/1/2019)

* log: added log-collector to collect event from JS
* api: RemoteServiceException uses original error message e.g. website->svc1->svc2, website will be able to display error message from svc2
* kafka: update to 2.2.0
* http: update okHTTP to 3.14.0
* search: update es to 6.7.0   
  !!! ES is forcing one index can only have one type (and naming it as "_doc"), you may need to rebuild index when upgrading to latest, refer to https://github.com/elastic/elasticsearch/pull/38270

### 6.11.2 (3/6/2019 - 3/15/2019)

* json: introduced Bean class to (de)serialize json with strict class type and bean validation, keep JSON loose to adapt any classes
* redis: support hash increaseBy
* cache: validate value from cache to prevent stale data

### 6.11.1 (2/8/2019 - 2/27/2019)   !!! this version has behavior and config API changes

* db: not allowing db entity with default value, due to it may cause undesired behavior on partialUpdate, ("new entity() -> change fields -> updatePartial(entity)" will update those fields with default value since they are not null)
* cache: stop supporting value and List<T> (considered as bad practice, better wrap with class for maintenance)        
  removed to register Cache<T> with name in CacheConfig, as it's generally for value type
* validator: simplify validator, use field name as part of error key (actual use cases use JS to validate anyway, if there is actual need, may add @Label to customize the error key)
* http: body bean must be registered via http().bean(), this only applies to beans used by raw controllers directly e.g. http().route(POST, "/ajax", bind(AJAXController.class)::ajax); http().bean(RequestBean.class, ResponseBean.class);
* search: update ES to 6.6.1
* db: close connection if statement is already closed due to previous error

### 6.10.10 (1/16/2019 - 1/31/2019)

* mongo: support enum as map key to be consistent with other subsystem
* json: JSON.fromJSON/toJSON added validation for app beans
* search: update ES to 6.6.0

### 6.10.9 (1/7/2019 - 1/16/2019)

* cookies: support SameSite attribute, and make SessionId cookie SameSite to prevent CSRF
* bean: support enum as Map key (mongo document is not supported yet)

### 6.10.8 (1/2/2019 - 1/4/2019)

* executor: use "TASK_REJECTED" error code when rejecting task during shutdown
* session: always try to save session even if on exception flow, in case of invalidate session or pass generated session id
