## TODO
* template, use ByteBuffer[] for performance tuning?
* web: get/form post, validate bean class and code generation for param serialization?
* general retry and throttling?
* webservice: client retry on network issue?
* website static content security check, (in server env, this is handled by nginx directly)
* validator: annotation for website, like @HTMLSafeString?
* cm: config management, dynamic update properties?
* cache: advanced feature: local/remote 2 level, async get on expiration, pre warm?
* template security check, escaping and etc
* http: make http session https only?
* think about /test//b, and /:path(*)
* framework error, queue listener, background task error notification?
* use smile for cache/queue?
* extend i18n message to validator/format/called by code directly

## Change log
### 4.3.1 (3/17/2016)
* http: added status code 410
* build: update gradle 2.12, flyway 4.0

### 4.3.0 (3/15/2016)
* mongo: support @MongoEnumValue
* db: renamed @EnumValue to @DBEnumValue

### 4.2.9 (3/11/2016)
* template: remove LanguageProvider as first step for i18n message support refactory

### 4.2.8-b1,b2,GA (3/9/2016 - 3/10/2016)
* stopwatch: start to use nanoTime
* log: actionlog start to trace cpuTime
* mongo: register enum codec from entity, for filter
* http: support statusCode 429
* rabbitmq: update to 3.6.1

### 4.2.7-b1,b2,b3,b4 (3/3/2016 - 3/8/2016)
* rabbitmq: make rabbitmq listener use dedicated thread pool
* executor: remove Batch<T> support
* scheduler: use dedicated thread pool
* search: remove client(), add analyze/index meta support
* mongo: added Mongo interface for unit test and management
* validation: support @Pattern
* mongo: disable cursorFinalizer, framework always close cursor
* thread: limit executor to processor*4 thread, to collect data for next phase design

### 4.2.6 (3/3/2016)
* config: update template and message config API
* web: support convert query param as value type

### 4.2.5 (3/2/2016)
* template: refine i18n language support

### 4.2.4 (2/29/2016 - 3/1/2016)
* mongo: removed old Mongo interface, refined mongo impl

### 4.2.3 (2/29/2016)
* mongo: add timeout to query, support MongoCollection<T>

### 4.2.2 (2/25/2016)
* mongo: updated to 3.2.2, added fongo support back
* search: exposed ElasticSearch as interface
* queue: tweak queue listener to result in less thread when rabbitmq is busy, track slow acknowledge

### 4.2.1 (2/24/2016)
* batch: allow specify maxConcurrentHandlers for batch
* async: provide thread name for all thread pool
* redis: update default redis timeout to 5s

### 4.2.0 (2/24/2016)
* validation: update validation exception message
* json: add from/to enum value to JSON, to support search query build with enum

### 4.1.9 (2/23/2016)
* util: ASCII supports char
* log: improve log error handling

### 4.1.8 (2/22/2016)
* fix: allow logger to log null message, e.g. errorMessage of NullPointerException

### 4.1.7 (2/17/2016 - 2/18/2016)
* rabbitmq: unified single/batch message polling

### 4.1.6 (2/17/2016)
* util: moved InputStreams to util, open to use
* log: make max trace log 3000, and only append warn/error after
* search: added SearchResponse<T> to convert hits to object on framework level

### 4.1.5 (2/16/2016)
* tuning: internal tuning, move low level optimization class to impl package, leave core.framework.util simple ones
* httpclient: use byte[] as body, remove ByteBuf
* log: use 500k as max trace log to forward to log processor, removed max trace event size limit, truncate after 5000 and only add warning events

### 4.1.4 (2/12/2016 - 2/15/2016)
* cache: make cache process byte[] directly to redis
* log: limit trace log 200k max per line, rise max log events per action to 10,000
* json: add afterburner module for object binding performance

### 4.1.3 (2/11/2016)
* db: lower default db timeout to 15s
* queue: updated rabbitMQ api and config api
* json: start convert json to bytes directly, to lower memory footprint with queue/ES/cache

### 4.1.2 (2/10/2016)
* web: removed web/not-found, web/method-not-allowed action assign, since we use error_code now
* background: moved pool-cleanup, collect-stat job to background thread, not included in action
* web: renamed all internal /management/* path to /_sys/*

### 4.1.1 (2/9/2016)
* monitor: initial monitoring draft, forward monitor metrics via logforwarder

### 4.1.0 (2/4/2016)
* template: invalid url attr will write src="", container will write empty if content is null
* elasticsearch: support 2.2.0, load groovy plugin in test context

### 4.0.9 (2/3/2016)
* redis: loose slow_redis warning threshold and timeout, on busy server due to CPU context switch, it's relative easy to hit it

### 4.0.8 (2/2/2016)
* web: requestURL now contains QueryString, (requestURL is url without decoding)

### 4.0.7 (1/28/2016)
* log: renamed all slow query error_code and naming
* pool: add error code POOL_TIME_OUT

### 4.0.6 (1/27/2016)
* template: warning if url fragment gets null url
* log: forward log one by one to simplify, batch happens on log-processor

### 4.0.5 (1/22/2016)
* log: make 3rd party log level to info, (e.g. ES log sampler error by INFO level in separated thread)
* elasticsearch: set ping timeout, support dynamic index name (for alias or time serial index name)

### 4.0.4 (1/21/2016)
* fix: typo in @NotEmpty/@ValueNotEmpty
* ws: support enum in path param

### 4.0.3 (1/19/2016)
* cache: update getAll to return Map<String, T>

### 4.0.2 (1/18/2016)
* web: removed URIBuilder, added Encodings.encodeURIComponent and decodeURIComponent
* web: use URI query param encoding/decoding to set/get cookie (refered as URLEncoding in other place, e.g. jquery)

### 4.0.1 (1/13/2016)
* schedule: support weekly and monthly trigger

### 4.0.0 (1/8/2016 - 1/11/2016)
* bytebuf: improve for skip/available as it will be wrapped by buffered stream or S3 client
* web: mark path not found error code to PATH_NOT_FOUND (to ignore 3rd party scan)
* batch: new async support, use Executor

### 3.10.1 (1/7/2016)
* mongo: support LocalDateTime in filter

### 3.10.0 (1/7/2016)
* httpclient: fix NPE with HttpEntity is null on 204

### 3.9.10 (1/5/2016)
* elasticsearch: ignore cluster name for transport client

### 3.9.9 (12/31/2015)
* url: encode '+' for path segment to keep compatible with other impl, e.g. undertow, AWS S3

### 3.9.8 (12/22/2015 - 12/24/2015)
* mongo: update driver to 3.2.0
* quality: support jacoco report

### 3.9.7 (12/18/2015 - 12/22/2015)
* lib: updated undertow to 1.3.10, jackson to 2.6.4, update quality check lib
* db: added repository.select(query) to support complex query + limit

### 3.9.6 (12/17/2015)
* log: dynamic group action log message to forward, 2000 or 5M which comes first

### 3.9.5 (12/16/2015 - 12/17/2015)
* log: update log forwarding structure, send in batch
* log: moved ErrorCode to core.framework.api.log.ErrorCode

### 3.9.4 (12/10/2015 - 12/14/2015)
* elasticsearch: update to 2.1.0
* log: restructure trace/warning log, for prod log aggregation

### 3.9.3 (12/9/2015)
* hash: added md5 support
* queue: removed sns/sqs support, only use rabbitmq from now on

### 3.9.2 (12/7/2015)
* redis/cache: mset with expiration, update jedis to 2.8.0

### 3.9.1 (12/4/2015)
* redis: fix the typo in log,
* bug: fix cache getAll/setAll

### 3.9.0 (11/24/2015 - 11/30/2015)
* http: added ACCEPTED(202) for async response code
* db: removed selectInt/selectLong/selectString, use selectOne instead with target view class, e.g. selectOne(sql, String.class, params)

### 3.8.9 (11/23/2015 - 11/24/2015)
* benchmark: added jmh for systemically performance turning
* util: added ASCII to handle ASCII chars
* web: validate empty pathParam on both server and client side
* util: fast impl of Strings.split(char)

### 3.8.8 (11/20/2015 - 11/23/2015)
* uri: simplify URIBuilder to match our use cases and performance tuning
* util: ByteBuf put byte[]
* refactory: performance tuning for html/uri encode/decode
* bug: fix html parser/lexer process emtpty end script tag.
* redis: update redis mget to return Map<String,String>
* route: validate path segment and variable

### 3.8.7 (11/20/2015)
* bug: fixed QueryParam encoding to encode +/?/=

### 3.8.6 (11/20/2015)
* bug: fixed URLFragment.isValidURL to allow '%'
* util: enhanced ByteBuf to make it can be used in broader scenario.

### 3.8.5 (11/19/2015)
* encodings: removed hex encoding, we don't have use case any more, use base64 instead
* encodings: removed url encoding from Encodings, added URIBuilder

### 3.8.4 (11/18/2015)
* cache: support get multiple keys in batch
* cache: changed get(Supplier) to get(Function<String,T>) to keep consistent with getAll, and be more functional style

### 3.8.3 (11/13/2015)
* bug: fix http content type parsing multipart
* search: support bulk index

### 3.8.2 (11/11/2015 - 11/12/2015)
* yml: removed yml support, use JSON instead
* mongo: removed fongo, updated mongo driver to 3.1.0

### 3.8.1 (11/11/2015)
* mongo: entity validation, not allow mix with JAXB

### 3.8.0 (11/10/2015)
* template: refactory html parser validation, validate boolean attribute

### 3.7.9 (11/9/2015)
* template: template must register in config before using, enforce safety
* template: validate empty attribute

### 3.7.8 (11/5/2015)
* template: refactory html parser, html validation, void element

### 3.7.7 (11/4/2015)
* config: support sys.http.port
* template: support i18n message with hierarchy, "en_US" fall back to "en"
* template: support dynamic href

### 3.7.6 (11/2/2015 - 11/3/2015)
* mongo: added get(id) method to replace findOne()
* mongo: added Query to support complex query

### 3.7.5 (11/2/2015)
* search: update to elasticsearch 2.0
* lib: update undertow to 1.3.4, jackson to 2.6.3
* http: introduced strong typed ContentType

### 3.7.4 (10/28/2015 - 10/29/2015)
* template: validate cdn attribute, url must start with /
* template: c:msg="" use key directly, removed expression support
* template: built-in i18n support
* properties: read in utf-8
* httpclient: fix text() to use utf-8 as encoding
* webservice: unified web service call error flow, RemoteServiceException provided structural info to be handled in client service app
* exception: provide ErrorCode support for error flow

### 3.7.3 (10/27/2015)
* template: removed c:cdn, automatically replace all src/href applies

### 3.7.2 (10/21/2015 - 10/27/2015)
* template: new template syntax, only support HTML template
* rabbitmq: publisher mark pool item to be broken if AlreadyClosedException (throws when exchange/queue is not configured correctly in RabbitMQ)
* site: exposed WebDirectory to injection context to provide way let website access web directory

### 3.7.1 (10/21/2015)
* mongo: support assigned id

### 3.7.0 (10/20/2015)
* mongo: config support
* lib: removed hsql to default test-compile scope, not all services had db.

### 3.6.9 (10/16/2015)
* gradle: core-ng app support ant property filtering, conf/env/web content override
* test: rewrite env conf validator, to verify /conf/env/resources and /conf/env/web
* web: #cdn function append version param

### 3.6.8 (10/14/2015)
* gradle: checkstyle update to 6.11.2, finally support intention for lambda
* web: static content supports file under root directly (like robot.txt and favicon.ico)
* web: static content removed 304 support, because static content is always handled by nginx in server env.

### 3.6.7 (10/12/2015 - 10/13/2015)
* web: update undertow to 1.3.0.Final
* db: improve setParam error message
* util: Files, added common functions

### 3.6.6 (10/09/2015)
* test: refactory test dependency and structure, support api().client() mock

### 3.6.5 (10/08/2015)
* web: support CDN config and #cdn() function, in sys.properties uses sys.cdn.host=
* template: expression validate return type, for for/if

### 3.6.4 (10/07/2015)
* validate: added @NotEmpty (for string), @ValueNotEmpty, @ValueNotNull (for collection)
* template: print location when expression failed
* template: removed #if(condition?a:b), always prefer to write method in model
* template: support single quote string

### 3.6.3 (9/28/2015 - 10/02/2015)
* template: add #if as built-in method for inline condition
* bug: fixed actionLog result is not updated
* bug: fixed web/IO allow content-length = 0, and catch all errors during IO in order to generate action log

### 3.6.2 (9/25/2015 - 9/28/2015)
* util: added ByteBuf for high performance network IO and NIO
* http-client: response bytes() changed to inputStream()

### 3.6.1 (9/21/2015 - 9/25/2015)
* web-service: moved core.framework.api.web.client.WebServiceRequestSigner to core.framework.api.web.service.WebServiceRequestSigner
* web: changed form parsing threading model, support file upload
* web: read json body in IO thread
* refactory: web-service code generator, logger

### 3.6.0 (9/18/2015 - 9/21/2015)
* config: better error message for load not found property
* lib: update all 3rd party lib up to date
* web-service: fix web service GET query, not append param if null
* log-forward: clear up queue if rabbitmq is down
* log: support to disable action/trace, !!! sys.properties, use sys.log.actionLogPath=console, sys.log.traceLogPath=console in dev env.

### 3.5.9 (9/17/2015 - 9/18/2015)
* queue: config, publish(String[] destinations) will call publish(String destination) if length = 1
* log: to protect log ES, not forward trace log if lines is more than max hold size = 5000, the log will still be written to file and application is responsible to split task into smaller chunk
* batch: tweak async executor for batch process
* redis: refined config, removed name support and added "sys.redis.host"
* search: config support, annotation support
* log-processor: due to search support changed, log processor must match latest core-ng

### 3.5.8 (9/16/2015)
* validate: type validator disallow cycle reference data structure (include same node as parent/child)
* redis: config support

### 3.5.7 (9/15/2015)
* redis: added setIfAbsent, use SET to replace SETEX
* search: removed parent/child support, always use nested from now on

### 3.5.6 (9/11/2015 - 9/14/2015)
* search: support parent/child

### 3.5.5 (9/11/2015)
* search: added delete and other tuning

### 3.5.4 (9/10/2015)
* bug: fixed DBConfig to handle multiple DB with different name
* search: added get()

### 3.5.3 (9/8/2015 - 9/9/2015)
* web: web bean validation, disallow to use @XmlEnum(Integer.class) on enum class, (in this case, not use enum, just use Integer, this is due to incomplete Jackson JAXB support)
* bug: fixed when rabbitmq server is down, the rabbitmq listener/log forward may keep trying without wait, cause cpu 100%
* httpClient: reduced default timeout to 1 min
* httpClient: disable cookie and auth by default
* util: InputStreams use 8K as buffer if length can not be determined, optimize for HTTPClient download with gzip

### 3.5.2 (9/6/2015 - 9/8/2015)
* tuning: various of tunings, utils/IO/random and etc
* util: removed Asserts, not really needed, in actual business code, just throw error/validation exception

### 3.5.1 (9/4/2015 - 9/6/2015)
* db: removed RowMapper, use view instead (broken change!)
* web: enum must have @XmlEnumValue

### 3.5.0 (9/3/2015 - 9/4/2015)
* config: renamed AbstractApplication to App (broken change!)
* db: removed repository select where clause must contains '?', exception is like "where some_column is null";
* db: removed Query, prefer use sql + param... (broken change!)
* web: updated Request.host() to Request.hostName(), according to url standard: http://bl.ocks.org/abernier/3070589
* db: replace repository insert/update query with dynamic code generation
* db: use @EnumValue to map the enum value in db (broken change!)

### 3.4.9 (9/1/2015 - 9/2/2015)
* db: lower the slow query to 5s and too many results to 1000
* redis: raise slow query to 200ms, (considering GC and network latency)
* redis: removed keys() (use SCAN in future if needed)
* redis: use binaryJedis and other changes according to profiling

### 3.4.8 (9/1/2015)
* db: fix selectInt/String with null returned

### 3.4.7 (8/30/2015 - 8/31/2015)
* search: validate search index document object
* validator: partial validate for update (ignore notNull)
* db: validate for update

### 3.4.6 (8/27/2015 - 8/28/2015)
* validation: added @Min/@Max
* db: removed db c3p0 pool, use internal pool

### 3.4.5 (8/27/2015)
* sysmodule: removed sys.jdbc.pool.minSize/maxSize, all env should be same, and if need to specify, put to App
* rabbitmq: check slow query

### 3.4.4 (8/25/2015 - 8/26/2015)
* rabbitmq: make listener use native thread
* redis: use internal pooling
* rabbitmq: channel pooling (send message perf is 20x faster if not closing channel)

### 3.4.3 (8/19/2015 - 8/25/2015)
* web: assign action for 404 => web/not-found, 405 => web/method-not-allowed
* scheduler: make scheduler for both internal and external scheduling
* db: use label not column name to map view

### 3.4.2 (8/19/2015)
* FIX: make ControllerInspector works with jdk 1.8.0_60

### 3.4.1 (8/17/2015 - 8/19/2015)
* rabbitmq: handle ShutdownSignalException gracefully
* lib: updated jackson/undertow/rabbitmq/httpclient lib
* FIX: ControllerInspector does not work with jdk 1.8.0_60, disable temporarily

### 3.4.0 (8/14/2015)
* log: refactor/tuning impl
* log: update log-processor ES mappings

### 3.3.9 (8/13/2015)
* ES: API changed to provide more flexibility to operate index and type
* log: draft of trace log forwarding
* http: fix put/post without content-type

### 3.3.8 (8/11/2015 - 8/12/2015)
* http server: interceptor ignore built-in controllers
* mongo: use LinkedHashMap to keep same order as in mongo
* log: draft of log forwarding

### 3.3.7 (8/11/2015)
* web: fix webservice controller inspection (getAnnotation/methodInfo)
* mongo: find supports orderby/skip/limit

### 3.3.6 (8/7/2015 - 8/10/2015)
* log: push action log to rabbitmq, index by ES/Kibana
* log: renamed action log requestId to id, and use refId for reference
* webService: WebContext can retrive request() to support web service
* rabbitmq: publish message with appId = -Dcore.appName
* webservice client: pass -Dcore.appName via "client" header

### 3.3.5 (8/6/2015)
* internal: expose web template manager for cms widget impl
* internal: renamed -Dcore.web to -Dcore.webPath, added -Dcore.appName, prepare for log aggregating

### 3.3.4 (8/5/2015)
* make default values to fit AWS medium/large instance, simplify env properties
* rabbitmq: make default user/password to rabbitmq/rabbitmq
* redis: make default pool size to (5,50)
* db: make default pool size to (5,50)

### 3.3.3 (8/4/2015)
* cache/session: update default redis pool size, (min=8,max=32) optimized to AWS medium/large instances.
* redis: print pool size info for slow query

### 3.3.2 (7/31/2015)
* updated c3p0 to 0.9.5.1 (all client must update c3p0 lib to use this version of core-ng)

  make c3p0 not use thread pool for checkin, we don't do test on checkin, this improves performance under high load

  dataSource.setForceSynchronousCheckins(true);

* updated mysql driver to 5.1.36 for db-migration

### 3.3.1 (7/30/2015)
* website: message support, site().message().messageProvider() is for custom message service

### 3.3.0 (7/29/2015)
* mongo: refactory, support fongo, minor API changes

### 3.2.9 (7/28/2015)
* queue: composite queue publisher
* queue: MessagePublisher.publish with routingKey, only support for RabbitMQ

### 3.2.8 (7/22/2015 － 7/23/2015)
* template: validate model class
* add TemplateEngine for general purpose
* module: renamed bind(supplier) to bindSupplier()
* template: "include" support
* queue: renamed MessagePublisher.reply to publish
* template: custom function support, prepare for #msg, #js, #css
* template: load from string

### 3.2.7 (7/21/2015 - 7/22/2015)
* db repository, added selectAll()
* template engine first draft impl, removed thymeleaf
* website static content

### 3.2.6 (7/17/2015 - 7/20/2015)
* refactory rabbitmq support
* update elasticsearch to 1.7

### 3.2.5 (7/16/2015)
* standardized validation exception and make validator throw validationException, refactory validator

### 3.2.4 (7/15/2015)
* fix: http response body validation pass empty list 
* databaseImpl, track available/total connections when getting conn from pool
* httpClient, allow server ssl cert change during renegotiation 

### 3.2.3 (7/14/2015)
* tune http client log info
* enhanced Files, create temp file, logging 

### 3.2.2 (7/13/2015)
* fix: api() client to pass "Accept: application/json" 
* replace URL/URLPath encoding with apache common codec
* tuned async task begin/end log message

### 3.2.1 (7/11/2015 - 7/13/2015)
* measure startup time
* measure time on test db schema creation

### 3.2.0 (7/9/2015)
* update gradle to 2.5
* update build gradle to publish to s3 directly

### 3.1.9 (7/8/2015)
* fix: api webservice client encode path param
* renamed StandardAppModule to SystemModule, and added jdbc pool properties

### 3.1.8 (6/30/2015)
* fix: requestURL(), parse x-forwarded-port to get requested port  
  
### 3.1.7.1 (6/30/2015)
* fix: test hsql db map BigDecimal to DECIMAL(10,2)

### 3.1.7 (6/26/2015 - 6/29/2015)
* refactory config structure to simplify test context
* refactory class validation into general one with spec
* WebContext to pass from interceptor

### 3.1.6 (6/25/2015)
* updated web customErrorHandler, moved it to http() and in top level of Module
* add requiredProperty() method in module

### 3.1.5 (6/24/2015)
* async task support, inject AsyncExecutor
* webservice support List as request/response, for channel-advisor and also for us 

### 3.1.4 (6/22/2015 - 6/23/2015)
* removed module.loadYML(), use YAML.load(ClasspathResources.text()) instead, for db init data, use YAML loadlist then call repository.batchInsert
* improve web service interface validator, better error message for bean param/path param missing @PathParam 
* Cache management controllers (list names/clear key) 

### 3.1.3 (6/22/2015)
* move repository() into db() and register db entity class to view. 

### 3.1.2 (6/22/2015)
* fix: webservice interface validator should allow String as @PathParam

### 3.1.1 (6/18/2015 - 6/22/2015)
* db added timeout for checkout conn and query, default is 60s
* update ElasticSearch API according to ES 1.6.0
* bind with supplier to support for expensive or external dependent object, test context can override by not calling supplier.get()
* mark trace in ActionLogContext, write trace log for all subsequent actions (apiCall/messages)
* create Application.configure()/start(), to make core-ng be used as script for testing/scripting 
* trace external calling time and count, e.g. db, redis

### 3.1.0 (6/15/2015)
* update trace log path

### 3.0.9 (6/15/2015)
* cache value type validator
* refactory cache impl
* Cache error handle, fault tolerant 

### 3.0.8
* database batchInsert/Delete
* httpClient supports download bytes
* webservice client

### 3.0.7
* support test initDB(), runscript and createSchema

### 3.0.6 (6/6/2015 - 6/8/2015)
* update rabbitmq/db config

### 3.0.5 (6/5/2015)
* refactory validator
* refactory module/config(testConfig)/builder/impl structure
* refactory session provider, local session cleanup
* added rabbitmq subscriber and listener

### 3.0.4 (6/4/2015)
* refactory module config, session changes to site().session()
* added core-ng-api for interface module
* updated row mapper, use code generation, updated Row interface

### 3.0.3 (6/3/2015)
* added core-ng-test support

### 3.0.2 (6/3/2015)
* YML supports type

### 3.0.1 (6/2/2015)
* support to add cache without name
* cache management controller
* add repository support