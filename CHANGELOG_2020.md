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
* db: added Repository.insertIgnore(entity) to handle duplicate key / constraint violation cases without catching UncheckedSQLException
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

