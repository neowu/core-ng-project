### 7.10.1 (12/13/2021 - 02/11/2022)

* search: update to es 7.17.0, high level rest client is deprecated, migrated to elasticsearch java client !!! Query API changed
  > refer to https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/introduction.html
  > the new API in my opinion abused java lambda, which added lots accidental complexity, and not jvm/gc friendly (although the bottleneck of es call usually is not on java side),   
  > in many cases it's actually much harder to use compare to old HLRC
* maven-repo: deleted all 6.x version except 6.13.9
  > recommend to upgrade to latest version
* monitor: support pagerduty (thanks Ajax for the contribution !!!)
* log: set kafka log appender "enable.idempotence" to false
  > since kafka 3.0.0, enable.idempotence is default to true, and it overrides "acks" to "all"
  > so this is set back to previous behavior, which means possible duplicate log messages if there is connection error
* api: allow using custom-built httpClient into api().createClient()
  > for regression ajax test use case, it can use stateful httpClient(enabled cookies)
* json: JSON.class won't allow null as json string or instance, to make sure always return non-null result
  > generally we never use "null" as json text, and it was triggering Intellij's warning (dereference of JSON.from() may produce NullPointerException)
* cache: removed redisLocal support, now cache only supports local or redis store
  > RedisLocalCacheStore (use pubsub to evict in memory keys) is not as useful as expected, and it also assumes cache redis is shared across services
  > it violates "share nothing" design principle, based on our experience, only few places requires high performance of local/in-memory cache
  > for those places, we can simply use local cache and kafka message to invalidate cache
  > and performance of redis cache is fast enough, usually 100 redis calls per action took less than 10ms
  > cache().add(name).local() for local store, for sensitive data or performance reason
* db: removed DBConfig.maxOperations() (default is 2000), added Database.maxOperations(threshold)
  > in real application, only very few actions do large number of db operations (e.g. replay, sync),
  > so added per action basis threshold
* db: check "*" for execute sql as well, for case like "insert into table select * from table"
* log: tweak trace log truncation, and added defensive logic to print actionLog/trace to console, if it's larger than kafka maxRequestSize
* kafka: update kafka to 3.1.0

### 7.9.3 (11/23/2021 - 12/10/2021)

* monitor: improve kube pod monitor error message for "Unschedulable" condition
* action: fixed webserviceClient/messagePublisher/executor only pass trace header when trace = cascade
* action: redesigned maxProcessTime behavior, use http client timeout and shutdown time out as benchmark
  > for executor task actions, use SHUTDOWN_TIMEOUT as maxProcessTime
  > for kafka listener, use SHUTDOWN_TIMEOUT as maxProcessTime for each message handling (just for warning purpose), ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG is still 30min
  > for http handler, use request timeout header or HTTPConfig.maxProcessTime(), (default to 30s)
  > for scheduler job, use 10s, scheduler job should be fast, generally just sending kafka message
* db: updated "too many db operations" check
  > if within maxProcessTime, it logs warning with error code TOO_MANY_DB_OPERATIONS, otherwise throws error
  > not break if critical workload generates massive db operations, and still protect from infinite loop by mistake
* db: added stats.db_queries to track how many db queries (batch operation counts as 1 perf_stats.db operation)
  > log-processor / kibana.json is updated with new diagram

### 7.9.2 (11/03/2021 - 11/22/2021)

* db: validate timestamp param must be after 1970-01-01 00:00:01
  > with insert ignore, out of range timestamp param will be converted to "0000-00-00 00:00:00" into db, and will trigger "Zero date value prohibited" error on read
  > refer to https://dev.mysql.com/doc/refman/8.0/en/insert.html
  > Data conversions that would trigger errors abort the statement if IGNORE is not specified. With IGNORE, invalid values are adjusted to the closest values and inserted; warnings are produced but the statement does not abort.
  > current we only check > 0, make trade off between validating TIMESTAMP column type and keeping compatible with DATETIME column type
  > most likely the values we deal with from external systems are lesser (e.g. nodejs default year is 1900, it converts 0 into 1900/01/01 00:00:00)
  > if it passes timestamp after 2038-01-19 03:14:07 (Instant.ofEpochSecond(Integer.MAX_VALUE)), it will still trigger this issue on MySQL
  > so on application level, if you can not ensure the range of input value, write your own utils to check before assigning
* jre: 17.0.1 released, published "neowu/jre:17.0.1"
* app: added external dependency checking before startup
  > it currently checks kafka, redis/cache, mongo, es to be ready (not db, generally we use managed db service created before kube cluster)
  > in kube env, during node upgrading or provision, app pods usually start faster than kafka/redis/other stateful set (e.g. one common issue we see is that scheduler job failed to send kafka message)
  > by this way, app pods will wait until external dependencies ready, it will fail to start if not ready in 30s
  > log kafka appender still treat log-kafka as optional
  > for es, it checks http://es:9200/_cluster/health?local=true
* http: Request.hostName() renamed to Request.hostname() to keep consistent with other places  !!! breaking change but easy to fix
* action: replaced ActionLogContext.trace() to ActionLogContext.triggerTrace(boolean cascade)
  > for audit context, we may not want to trace all correlated actions, with this way we can tweak the scope of tracing
* app: startupHooks introduced 2 stages (initialize and start), removed lazy init for kafka producer / elasticsearch / mongo
  > since client initialize() will be called during startup, it removes lazy init to simplify
  > if you want to call Mongo/ElasticSearch directly (e.g. local arbitrary main method), call initialize() before using
* log-processor: removed elasticsearch appender support
  > in prod env, we use null log appender for log-processor, since log-processor is stable, and not necessary to double the amount of action logs in log-es
  > if anything wrong happened, error output of log-processor is good enough for troubleshooting

### 7.9.1 (10/22/2021 - 11/03/2021)

* site: StaticDirectoryController will normalize path before serving the requested file, to prevent controller serving files outside content directory
  > it's not recommended serving static directory or file directly through java webapp,
  > better use CDN + static bucket or put Nginx in front of java process to server /static
* db: use useAffectedRows=true on mysql connection, return boolean for update/delete/upsert !!! pls read this carefully
  > refer to https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-connp-props-connection.html#cj-conn-prop_useAffectedRows
  > the MySQL affected rows means the row actually changed,
  > e.g. sql like update table set column = 'value' where id = 'id', the affected row = 0 means either id not found or column value is set to its current values (no row affected)
  > so if you want to use update with optimistic lock or determine whether id exists, you can make one column always be changed, like set updatedTime = now()
* db: added Repository.upsert() and Repository.batchUpsert()
  > upsert is using "insert on duplicate key update", a common use case is data sync
  > !!! due to HSQL doesn't support MySQL useAffectedRows=true behavior, so upsert always return true
  > we may create HSQL upsert impl to support unit test if there is need in future (get by id then create or update approach)
* db: Repository update and delete operations return boolean to indicate whether updated
  > normally we expect a valid PK when updating by PK, if there is no row updated, framework will log warning,
  > and the boolean result is used by app code to determine whether break by throwing error
* db: removed DBConfig.batchSize() configuration
  > with recent MySQL server and jdbc driver, it is already auto split batch according to max_allowed_packet
  > refer to com.mysql.cj.AbstractPreparedQuery.computeBatchSize
  > and MySQL prefers large batch as the default max_allowed_packet value is getting larger
* db: mysql jdbc driver updated to 8.0.27
* redis: added Redis.list().trim(key, maxSize)
  > to make it easier to manage fixed length list in redis

### 7.9.0 (09/29/2021 - 10/21/2021)  !!! only support java 17

* jdk: updated to JDK 17
  > for local env, it's easier to use intellij builtin way to download SDK, or go to https://adoptium.net/
  > adoptium (renamed from adoptopenjdk) doesn't provide JRE docker image anymore, you should build for yourself (or use JDK one if you don't mind image size)
  > refer to docker/jre folder, here it has slimmed jre image for generic core-ng app
* message: make Message.get() more tolerable, won't fail but log as error if key is missing or language is null
  > use first language defined in site().message() if language is null
  > return key and log error if message key is missing,
  > with integration test context, still throw error if key is missing, to make message unit test easier to write
* http: update undertow to 2.2.12
* actionLog: added ActionLogContext.trace() to trigger trace log
  > e.g. to integrate with external services, we want to track all the request/response for critical actions
  > recommended way is to use log-processor forward all action log messages to application kafka
  > then to create audit-service, consume the action log messages, save trace to designated location (Cloud Storage Service)
* action: removed ActionLogContext.remainingProcessTime(), and httpClient retryInterceptor won't consider actionLog.remainingProcessTimeInNano
  > it's not feasible to adapt time left before making external call (most likely http call with timeout),
  > due to http call is out of control (e.g. take long to create connection with external site), or external sdk/client not managed by framework
  > so it's better to careful plan in advance for sync chained http calls
  > maxProcessTime mechanism will be mainly used for measurement/visibility purpose (alert if one action took too long, close to maxProcessTime)

### 7.8.2 (09/20/2021 - 09/28/2021)

* java: target to Java 16
  > since all projects are on java 16 for long time, this should not be issue, will update to java 17 LTS soon
* kafka: update client to 3.0.0
* es: update to 7.15.0
* db: added "boolean partialUpdate(T entity, String where, Object... params)" on Repository, to support updating with optimistic lock
  > to clarify, Repository.update() must be used carefully, since it's update all columns to bean fields, regardless it's null
  > in actual project, common use cases generally are like to update few columns with id or optimistic lock, so always prefer partialUpdate over update
  > for accumulated update (like set amount = amount + ?), it's still better use Database.execute() + plain sql
* db: updated Repository.batchInsertIgnore to return boolean[], to tell exactly whether each entity was inserted successfully

### 7.8.1 (08/19/2021 - 09/14/2021)

* db: batchInsert returns Optional<long[]> for auto incremental PK
* db: update mysql driver to 8.0.26
* httpClient: support client ssl auth
* site: removed Session.timeout(Duration), it proved not useful, for app level remember me, it's better handle in app level with custom token
* redis: support password auth for redis/cache
* ws: added WebContext.responseCookie to allow WS to assign cookie to response

### 7.8.0 (08/04/2021 - 08/17/2021)   !!! breaking changes, pls read details

* es: update to 7.14.0
* log-processor: kibana 7.14 added duration human precise formatter, updated all time fields of index pattern
  > must update kibana/es to 7.14 to use this version of log-processor
* api: always publish /_sys/api, for internal api change monitoring
* api: added /_sys/api/message to publish message definition, for future message change monitoring
* error: refactored ErrorResponse and AJAXErrorResponse, !!! changed ErrorResponse json field from "error_code" to "errorCode"
  > !!! for consistency, it breaks contract, it's transparent if both client/server upgrade to same framework version, ErrorResponse only be used when client is from coreng
  > if client uses old version of framework, RemoteServiceException will not have errorCode
  > ErrorResponse is renamed to InternalErrorResponse, AJAXErrorResponse is renamed to ErrorResponse
* api: changed system property "sys.publishAPI.allowCIDR" to "sys.api.allowCIDR", !!! update sys.properties if this is used

### 7.7.5 (07/20/2021 - 07/26/2021)

* mongo: updated driver to 4.3.0
* action: added ActionLogContext.remainingProcessTime() for max time left for current action, to control external calling timeout or future get with timeout
* http: update undertow to 2.2.9

### 7.7.4 (06/23/2021 - 07/16/2021)

* site: added Session.timeout(Duration), to allow application set different timeout
  > e.g. use cases are like longer mobile app session expiration time, or "remember me" feature
* http: disabled okHTTP builtin retryOnConnectionFailure, use RetryInterceptor to log all connection failures explicitly, more failure case handling

### 7.7.3 (06/14/2021 - 06/22/2021)

* log: fixed error when third-party lib calls slf4f logger with empty var args (Azure SDK)
* executor: updated way to print canceled tasks during shutdown (with JDK16, it cannot access private field of internal JDK classes)
* log-processor: add first version of action flow diagram, pass actionId to visualize entire action flow with all related _id and correlation_id
  > e.g. https://localhost:8443/diagram/action?actionId=7A356AA3B1A5C6740794

### 7.7.2 (06/03/2021 - 06/14/2021)

* monitor: fixed overflowed vmRSS value, use long instead of int
* api: added "app" in APIDefinitionResponse
* monitor: api config json schema changed !!!
  > changed from map to List<ServiceURL>, to simplify config. it requires the latest framework, refers to above
    ```json
        {
          "api": {
            "services": ["https://website", "https://backoffice"]
          } 
        }
    ```

### 7.7.1 (05/25/2021 - 06/02/2021)

* log-processor/kibana: added http server/client dashboard and visualizations (http / dns / conn / reties / delays)
* http: added "action.stats.http_delay" to track time between http request start to action start (time spent on HTTPIOHandler)
  added action.stats.request_body_length/action.stat.response_body_length to track
* httpClient: track request/response body length thru perf_stats.http.write_entries/read_entries
* db: fixed insertIgnore reports write_entries wrong, mysql 8.0 return SUCCESS_NO_INFO (-2) if insert succeeds

### 7.7.0 (04/26/2021 - 05/25/2021)

* api: replaced /_sys/api, to expose more structured api info
  > one purpose is to create api monitoring, to alert if api breaks backward compatibility
  > !!! for ts client code generator, refer to https://github.com/neowu/frontend-demo-project/blob/master/website-frontend/webpack/api.js
* redis: support pop with multiple items
  > !!! only be supported since redis 6.2, use latest redis docker image if you use this feature
  > pop without count still uses old protocol, so it's optional to upgrade redis
* monitor: support to monitor api changes, alert config json schema changed !!!
  > refer to ext/monitor/src/test/resources/monitor.json, ext/monitor/src/test/resources/alert.json for example config
  > !!! alert config -> channels changed, to support one channel with multiple matchers, e.g.
  ```json 
    "notifications": [
        {"channel": "backendWarnChannel", "matcher": {"severity": "WARN", "indices": ["trace", "stat"]}},
        {"channel": "backendErrorChannel", "matcher": {"severity": "ERROR", "indices": ["trace", "stat"]}},
        {"channel": "frontendWarnChannel", "matcher": {"severity": "WARN", "indices": ["event"]}},
        {"channel": "frontendWarnChannel", "matcher": {"severity": "WARN", "errorCodes": ["API_CHANGED"], "indices": ["stat"]}},
        {"channel": "frontendErrorChannel", "matcher": {"severity": "ERROR", "indices": ["event"]}},
        {"channel": "frontendErrorChannel", "matcher": {"severity": "ERROR", "errorCodes": ["API_CHANGED"], "indices": ["stat"]}},
        {"channel": "additionalErrorCodeChannel", "matcher": {"apps": ["product-service"], "errorCodes": ["PRODUCT_ERROR"]}}
    ]
  ```
* log-processor: updated kibana objects to be compatible with kibana 7.12.0, rebuild objects with kibana object builder
  > refer to core-ng-demo-project/kibana-generator

* es: update to 7.13.0, updated ElasticSearch.putIndexTemplate impl to use new PutComposableIndexTemplateRequest
  > !!! refer to https://www.elastic.co/guide/en/elasticsearch/reference/current/index-templates.html
  > must update index template format to match new API, refer to ext/log-processor/src/main/resources/index/action-index-template.json as example

### 7.6.15 (04/13/2021 - 04/25/2021)

* log-processor: support to forward action-log/event to another kafka for data warehouse sink
  > !!! env vars are now starts with APP_, e.g. APP_KIBANA_URL, APP_KIBANA_BANNER (needs to update existing config)
  > configure by env APP_LOG_FORWARD_CONFIG, kube example:
    <pre>
      - name: APP_LOG_FORWARD_CONFIG
        value: |
            {
                "kafkaURI": "kafka-0.kafka",
                "action": {
                    "topic": "action",
                    "apps": ["website", "mobile-api"],
                    "ignoreErrorCodes": ["FORBIDDEN", "PATH_NOT_FOUND", "UNAUTHORIZED", "METHOD_NOT_ALLOWED"]
                }
            }
    </pre>
* log-processor: arch diagram supports excludes query param
  > generally many lines are caused by backend-test-service or regression-test-service, now it can be excluded to simplify diagram
  > e.g. https://localhost:8443/diagram/arch?excludes=backend-test-service,regression-test-service
* internal: support java 16
  > still will be released under java 15, runtime can update to java 16 first (build server, app docker base image)
  > !!! to use adoptopenjdk/openjdk16:alpine-jre, must add following to your Dockerfile, the kafka/snappy lib requires it to load native lib
  > RUN apk add --no-cache gcompat
* kafka: update to 2.8.0
  > 2.8.0 client works fine with 2.7.0 broker
  > in docker/kafka docker compose, it shows kafka kraft preview usage, kafka without zookeeper
* monitor: add kafka used disk metrics / dashboard, alert
  > added "highDiskSizeThreshold" in kafka monitor config, use absolute size, not percentage
  > refer to ext/monitor/src/test/resources/monitor.json to example config

### 7.6.14 (03/18/2021 - 04/13/2021)

* search: update es to 7.12.0
* action: change error "action log context value too long" to warning
* mongo: MongoMigration supports overwriting property by env
* es: ElasticSearchMigration supports overwriting property by env
* log-processor: generate system arch diagram
  > call https://log-processor:8443/diagram/arch?hours=24, use port-forward in kube env, hours param is optional
  > click nodes and edges to show extra info in tooltip

### 7.6.13 (03/01/2021 - 03/18/2021)

* mongodb: update driver to 4.2.2
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
* db: allow using "multiply operator" (not wildcard) in sql, e.g. select column*3 from table

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

