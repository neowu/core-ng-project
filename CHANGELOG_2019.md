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

### action-log will be sent to new topic (action-log-v2), to avoid error during transition, only impact could be some old action-log is not indexed which is minor

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
