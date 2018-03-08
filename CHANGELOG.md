## Change log
### 6.0.1 (3/7/2018 - )
* kafka: update to 1.0.1

### 6.0.0 (3/4/2018 - 3/6/2018)     !!! only support Java 9+
* jdk: drop java 8 support
* log: removed write action/trace to file, updated sys.properties log key to sys.log.appender
        sys.log.appender=console => write action/trace to console
        sys.log.appender=kafkaURI => forward log to kafka
       (in cloud env, console logging is prefered no matter it's docker or systemd/journald, or use log forwarding) 
* http: update undertow to 2.0.1

### 5.3.8 (2/28/2018 - 3/4/2018)    !!! 5.3.X is last version to support Java 8
* http: limit max requestURL length to 1000
* search: update es to 6.2.2

### 5.3.7 (2/24/2018 - 2/27/2018)
* http: not handling /health-check as action anymore, means /health-check will not be part of action log, to reduce noisy in kube env
* httpClient: removed HTTPRequest static method shortcut, use new HTTPRequest(method, uri) instead, to enforce consistent api style 
* api: tweak api generation

### 5.3.6 (2/14/2018 - 2/20/2018)
* redis: support increaseBy
* api: tweak api generation to fit client impl, refer to https://github.com/neowu/frontend-demo-project/blob/master/website-frontend-ts/src/service/user.ts as example

### 5.3.5 (2/12/2018 - 2/14/2018)
* inject: bind(object) will inject object, to make it easier to register bean with both manual wired and autowired dependencies
* properties: removed support of loading properties from file path, for kube we will using env/jvm argument overriding
* httpClient: add basic auth support
* search: update es to 6.2.1

### 5.3.4 (2/5/2018 - 2/11/2018)
* http: add ContentType.IMAGE_PNG constant, (e.g. used by captcha controller)
* api: change returned content type to javascript, make it easier to view by browser
* test: removed core.framework.test.EnvWebValidator, since all static file/content will be handled in frontend project, copy the impl to your own project if you still need
* http: set max entity size to 10M, to prevent from large post body
* http: support text/xml as body
* http: change ip whitelist to cidr to support subnet

### 5.3.3 (2/1/2018 - 2/4/2018)
* api: support configure api client timeout and slow operation threshold (default is 30s and 15s)
* log: add redis read/write entries tracking, index read/write entries as null if not set  
* search: update es to 6.1.3
* api: add "_sys/api" to return typescript definition, used by frontend

### 5.3.2 (1/29/2018 - 2/1/2018)
* html: added "autofocus", "allowfullscreen", "hidden" & "async" to boolean attributes
* api: remove openapi impl, not useful in actual development life cycle, we plan to impl tool to generate typescript ajax client from interface directly 
* api: temporary removed: update API client with 30s timeout and 15s slow operation threshold (will support configure in next version)

### 5.3.1 (1/28/2018 - 1/29/2018)
* search: update es to 6.1.2
* html: added "required", "sortable" to boolean attributes

### 5.3.0 (1/22/2018 - 1/23/2018)
* http: added gzip support, added cache param for static content 
* api: update API client with 30s timeout and 15s slow operation threshold

### 5.2.10 (1/15/2018 - 1/17/2018)
* http: support ip whitelist(due to gcloud public LB does not support ingress IP restriction), update undertow to 1.4.22
* db: change mysql jdbc driver to com.mysql.cj.jdbc.Driver (works with mysql:mysql-connector-java:6.0.6), old one is deprecated, https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-api-changes.html
* test: update junit to 5.0.3 and tweak dependency

### 5.2.9 (12/26/2017 - 1/10/2018)
* search: update es to 6.1.1
* http: support patch method, for partial update webservice, e.g. update status for one entity

### 5.2.8.1 (12/20/2017)
* jdk: republish with jdk 8, due to https://github.com/neowu/core-ng-project/issues/8

### 5.2.8 (12/20/2017)
* property: when override property with env var, convert key with upper case and replace '.' with '_', e.g. "sys.kafka.uri" to "SYS_KAFKA_URI", 
    due to dot is not supported by POSIX, especially not supported by alpine 3.6, https://bugs.alpinelinux.org/issues/7344 

### 5.2.7 (12/11/2017 - 12/19/2017)
* property: allow all the properties can be override by env var (mount by kubernetes/docker) or system property (via -Dkey=value)
* search: update es to 6.1.0, disable zen for integration test

### 5.2.6 (12/05/2017 - 12/11/2017)
* db: fixed ZonedDateTime saves to DB with nano precision
* search: update es to 6.0.1
* api: support /_sys/api, can be used by http://editor.swagger.io/ to generate OpenAPI doc

### 5.2.5 (11/21/2017 - 12/04/2017)
* test: replace hamcrest with assertj
* http: in local dev env, allow developer to run multiple apps on different port, either put -Dsys.http.port=8080 in Intellij Run configuration, or ./gradlew -Dsys.http.port=8080 :some-service:run

### 5.2.4 (11/20/2017 - 11/20/2017)
* test: update junit to 5.0.2
* bug: fixing elasticsearch client does not need EsExecutors.PROCESSORS_SETTING settings   

### 5.2.3 (11/17/2017 - 11/20/2017)
* sys: added Threads.availableProcessors() to allow use -Dcore.availableProcessors to specify cpu core to be used 
        due to in docker/kubenetes env, Runtime.getRuntime().availableProcessors() always return number of cores from host, not cpu limited by cgroup (-cpus or limit.cpus).
* json: support empty object

### 5.2.2 (11/14/2017 - 11/15/2017)
* log: console logger writes to stderr for WARN/ERROR, this is to help kubernetes logger driver to classify log severity, e.g. stackdriver in gcloud   
* search: update es to 6.0.0

### 5.2.1 (11/8/2017 - 11/13/2017)
* log: log-processor collects its own cpu/heap/kafka stats to index
* log: ActionLogContext.stat() supports adding up
* log: ActionLogContext.track() tracks I/O reads and writes for heavy backend db, such as DB/Mongo/ES
* web: put max forwarded ips config to prevent from x-forwarded-for clientIp spoofing
        http().maxForwardedIPs() 
* search: update es to 5.6.4        

### 5.2.0 (10/28/2017 - 11/7/2017)
* bean: removed "javax.inject:javax.inject:1", replaced with core.framework.inject, removed constructor injection support (to simplify and prepare for JDK 9) 
* jdk: make built target compatible with JDK 9, as first step of java 9 migration (some of toolchain does not support java 9 yet) 
* kafka: update to 1.0.0
* pool: monitor pool size for both db and redis/cache/session

### 5.1.1 (10/17/2017 - 10/27/2017)
* pool: refactor and simplify resource pool
* web: @QueryParam bean validation to disallow @Property
* httpClient: changed httpClient to interface, to make it easier to mock/override binding, change HTTPRequest to bean style from builder style
              you need to update the binding to "bind(HTTPClient.class, new HTTPClientBuilder().build())"              
* redis: replaced jedis impl with minimal support
        we only need to support request/response/pipeline model, and due to we manage resource pool, cluster support requires customization anyway
* check: replace findbugs with spotbugs

### 5.1.0 (10/13/2017 - 10/16/2017)
* test: updated to junit 5, for old tests before upgrading API, add following dependency 'junit:junit:4.12' 'org.junit.vintage:junit-vintage-engine:4.12.1'
        for integration test use "@ExtendWith(IntegrationExtension.class)" instead of @RunWith
* search: update es to 5.6.3

### 5.0.0 (10/13/2017)
* api: remove jaxb dependency, use our own @Property instead (jaxb will be deprecated by jdk9 and it has unnecessary java.desktop module dependency)
* package: move core.framework.api from core-ng lib to core.framework, due to with JDK 9, package must be unique to export across all libs, this is to prepare for JDK 9 modules

### 4.16.4 (10/2/2017 - 10/13/2017)
* web: enable http2 support
* web: simplified ControllerInspector, to only support JDK 1.8.0_60 or later
* lib: update javaassist to 3.22, kafka to 0.11.0.1

### 4.16.3 (9/5/2017 - 9/30/2017)
* mongo: tweak mongo encoder and decoder code gen   
* http: update undertow to 1.4.20, tweak the cookies config
* search: update es to 5.6.2
* web: simplified Response interface, use chained method to set content-type and status going forward

### 4.16.2 (9/1/2017 - 9/4/2017)
* bug: fix oracle pagination query param value

### 4.16.1 (8/29/2017 - 9/1/2017)
* validate: replace validation impl with dynamic code generation
* kafka: combine json reader/validator into handler, since one topic can only have one message class and there is no rabbitMQ anymore
* test: added EnumConversionValidator to facilitate verifying view enum to domain enum conversion

### 4.16.0 (8/23/2017 - 8/29/2017)
* api: add validation for GET/DELETE body type
* web: added @QueryParam support to replace flat JAXB bean for GET/DELETE

### 4.15.0 (8/17/2017 - 8/22/2017)
* queue: removed rabbitMQ support, make kafka only queue implementation
* search: update es to 5.5.2
* web: support @ResponseStatus on exception class, to simplify default error response handling
* web: updated request parser to determine port with x-forwarded-proto aware, (due to google cloud level 7 lb does not forward x-forwarded-port)

### 4.14.0 (8/11/2017 - 8/16/2017)
* mongo: update driver to 3.5, and its new builtin POJO impl should be slower than coreng due to reflection, so we still keep EntityCodec
* db: replaced repository.select(Query) with Query repository.select() to support dynamic query with pagination

### 4.13.0 (8/4/2017 - 8/11/2017)
* web: Response.file(File) changed to Response.file(Path) to be consistent with entire api design, (to return path via WebDirectory)  
* json: update jackson to 2.9.0, simplified configuration
* scheduler: add secondly trigger, to support precisely scheduling to align with clock time 

### 4.12.7 (8/1/2017 - 8/3/2017)
* search: update es to 5.5.1
* db: update insertQuery to use prepareStatement(String sql, String columnNames[]), to adapt to both mysql, oracle and potentially postgreSQL

### 4.12.6 (7/26/2017 - 7/30/2017)
* db: support oracle db insert with sequence

### 4.12.5 (6/13/2017 - 7/26/2017)
* search: update es to 5.5.0
* kafka: update to 0.11.0.0, use built-in headers for meta data

### 4.12.4 (5/15/2017 - 6/13/2017)
* test: validate override binding in integration test
* lib: update es to 5.4.1, fongo to 2.1.0
* web: forbidden inherited controller 
* html: recommend to use double quote to delimit attribute value

### 4.12.3 (5/9/2017 - 5/10/2017)
* search: update es to 5.4.0, delete by query support
* httpclient: changed to disable redirect by default, support to enable redirect handling
* httpclient: added 302 Found status

### 4.12.2 (4/19/2017 - 5/1/2017)
* web: action log uses context.requestURL instead of context.path
* route: remove regex path variable support, not used anymore

### 4.12.1 (4/10/2017 - 4/18/2017)
* api: rename WebServiceRequestSigner to WebServiceRequestInterceptor
* rate: added rate limiter support for API and site
* log: add actionLogContext.stat() to support numeric context field, for analytics thru kibana

### 4.11.3 (4/4/2017 - 4/10/2017)
* scheduler: daily/weekly/monthly supports timezone

### 4.11.2 (4/3/2017 - 4/4/2017)
* search: update es to 5.3.0
* db: fix to support ZonedDateTime
* site: enable security headers, X-Frame-Options, X-XSS-Protection, X-Content-Type-Options

### 4.11.1 (3/20/2017 - 3/21/2017)
* kafka: updated maxPoll and minPoll config

### 4.10.15 (3/16/2017 - 3/20/2017)
* kafka: kafka().publish() return publisher instance for convenience
* kafka: add default publish method to use UUID as key

### 4.10.14 (3/14/2017)
* mongo: fix bulkReplace to set upsert

### 4.10.13 (3/10/2017 - 3/14/2017)
* gradle: update to 3.4.1, updated lib.gradle
* template: support to use html tag contains '-'

### 4.10.12 (3/10/2017)
* kafka: fix LONG_PROCESS threshold calculation

### 4.10.11 (3/9/2017)
* mongo: tweak bulkDelete param type
* kafka: update default consumer behavior to auto reset to latest

### 4.10.10 (3/7/2017 - 3/9/2017)
* utils: make Exceptions.error be aware of last Throwable argument, keep it consistent as logger
* mongo: add bulkDelete

### 4.10.9 (3/3/2017 - 3/7/2017)
* lib: update kafka to 0.10.2.0, es to 5.2.2
* kafka: simplify listener, warn if took too long to consume message

### 4.10.8 (2/28/2017 - 3/2/2017)
* api: validate webservice impl method should not have @PathParam()
* bug: fixed if/for statement template statement pattern, thanks @bitmore88gt and @julioalberto64

### 4.10.7 (2/27/2017 - 2/28/2017)
* kafka: log info on message poll
* kafka: monitor max commit latency

### 4.10.6 (2/27/2017)
* kafka: support max poll records

### 4.10.5 (2/22/2017 - 2/27/2017)
* config: refactory module/config
* bind: removed bindSupplier, not used anymore
* queue: deprecate rabbitMQ
* session: make session only supports https, with modern standard, https should be by default
* http: StaticDirectoryController checks requested file must under directory

### 4.10.4 (2/14/2017 - 2/21/2017)
* kafka: set default max process time to 15 mins
* lib: update undertow to 1.4.10, es to 5.2.1
* config: better validate kafka/mongo/es uri property presents

### 4.10.3 (2/13/2017)
* kafka: support max process time for long process

### 4.10.1 (2/3/2017)
* kafka: tweak kafka producer and consumer stats

### 4.10.0 (2/1/2017 - 2/3/2017)
* kafka: track publish, collect kafka producer and consumer stats

### 4.9.0 (1/25/2017 - 2/1/2017)
* undertow: update to 1.4.8
* inject: fix beanFactory inject method parameter to support generic type
* search: update ES to 5.2.0

### 4.8.9 (1/12/2017 - 1/17/2017)
* kafka: update logging to expose more info
* mongo: check entities must not be empty in bulk operations
* redis: support scan keys

### 4.8.8 (1/9/2017 - 1/12/2017)
* kafka: update to 0.10.1.1
* mongo: support bulk replace, update driver to 3.4.1

### 4.8.7 (12/12/2016 - 1/3/2017)
* search: update es to 5.1.1
* mongo: support bulk insert
* kafka: add kafka support in order to replace rabbitMQ
* properties: add _sys property controller for troubleshooting
* properties: allow load properties from file path, for kube/docker support

### 4.8.6 (12/6/2016)
* http: change https redirection to 301 instead of 308

### 4.8.5 (12/5/2016 - 12/6/2016)
* http: support site.httpsOnly(), for https hsts and https redirect
* redis: update jedis to 2.9.0

### 4.8.4 (11/16/2016 - 12/1/2016)
* search: update es to 5.0.2
* http: added https with self signed cert support, make http/https port configurable

### 4.8.3 (10/27/2016 - 11/16/2016)
* search: update es to 5.0.0
* log: removed rabbitmq log forward support
* session: replace redis impl with HASH, to make it easier to manage and share between multiple apps
* web: fix path param to decode %2F to '/', (disabled undertow decodeURL completely)

### 4.8.2 (10/26/2016)
* log: rename kafkaHost to kafkaURI

### 4.8.1 (10/26/2016)
* http: fix decode path param bug

### 4.8.0 (10/20/2016 - 10/24/2016)
* kafka: update to 0.10.1.0
* test: fix EnvWebValidator to support node
* template: translate <template> to <script type="text/template"/> to support IE

### 4.7.9 (10/19/2016 - 10/20/2016)
* cdn: removed version support, use node pipeline instead
* web: use ./src/main/web as web directory if node is used

### 4.7.8 (9/15/2016 - 10/17/2016)
* lib: update undertow to 1.4.3
* http: url param supports boolean
* search: update esTookTime to nano seconds
* mockito: update mockito to 2.1
* queue: start supporting kafka
* mongo: removed eval() support
* log: make logger.isDebugEnabled() to return false, to disable 3rd party log gracefully

### 4.7.7 (9/14/2016 - 9/15/2016)
* http: throw methodNotAllowedException for unknown http method
* session: ignore all errors on decoding redis session value
* session: allow configure session cookie name

### 4.7.6 (9/13/2016)
* session: change session data encoding to JSON

### 4.7.5 (9/7/2016 - 9/8/2016)
* search: update jackson lib and es lib to match latest
* json: remove optional field support, which is not useful, only support Optional<T> as return object
* search: update client to enable sniff

### 4.7.4 (8/31/2016 - 9/1/2016)
* mongo: updated driver to 3.3.0
* convert: support ZonedDateTime in mongo/db/json conversion

### 4.7.3 (8/29/2016)
* template: support c:html:attribute for attribute not be escaped (thanks gabo)

### 4.7.2 (8/15/2016 - 8/16/2016)
* site: expose messages interface for i18n messages
* gradle: remove properties process, use node/gulp asset pipeline instead
* redis: restructure hash() and set() api

### 4.7.1 (8/11/2016)
* monitor: collect gc stats in CollectStatTask

### 4.7.0 (8/4/2016 - 8/5/2016)
* http: update undertow to 1.4.0
* test: support MockExecutor

### 4.6.9 (7/11/2016 - 8/1/2016)
* search: support bulkDelete
* redis: support hget

### 4.6.8 (7/6/2016)
* test: EntitySchemaGenerator support LocalDate

### 4.6.7 (7/5/2016)
* db: support LocalDate type as column

### 4.6.6 (7/1/2016)
* bind: use mock executor in integration test

### 4.6.5 (6/29/2016)
* mongo: support readPreference in all operation

### 4.6.4 (6/28/2016)
* search: support search type in ES

### 4.6.3 (6/24/2016)
* hash: added SHA1 SHA256 support

### 4.6.2 (6/22/2016 - 6/23/2016)
* search: remove groovy test support, actually script query is never useful, not plan to use anymore
* mock: mockRedis supports all operations
* search: support foreach for reindex support

### 4.6.1 (6/22/2016)
* httpclient: ContentType.parse supports to ignore illegal charset
* session: support to configure cookie domain

### 4.6.0 (6/20/2016 - 6/21/2016)
* mongo: refine mongo decoding logic and error message
* http: fix webContext should init at beginning, to make it available in 404 error handler

### 4.5.9 (6/10/2016)
* mongo: support connect to multiple mongo with name
* mongo: support MapReduce/Aggregate with readPreference
* httpclient: wrap UncheckedIOException as HTTPClientException

### 4.5.8 (6/8/2016)
* queue: update queue handling action to be queue/{queue}/{message_type}, to make it easier to manage and analyze

### 4.5.7 (6/7/2016)
* template: support classpath template (for include)

### 4.5.6 (6/3/2016)
* session: remove key

### 4.5.5 (5/25/2016)
* httpclient: throw HTTPClientException when status code is not supported

### 4.5.4 (5/24/2016)
* template: fix template engine with c:src in String template

### 4.5.3 (5/23/2016)
* validate: support Optional<T>
* validate: removed @ValueNotNull/@ValueNotEmpty/@ValuePattern, use @NotNull/@NotEmpty/@Pattern to simplify

### 4.5.2 (5/20/2016)
* mongo: count() supports null filter
* background-task: fix task can be broken if exception occurred

### 4.5.1 (5/18/2016)
* error: fix errorCode.severity handling by remoteServiceException
* mongo: support mapReduce

### 4.5.0 (5/9/2016)
* web: support return all values for file upload and form post
* web: support PUT for file uploading

### 4.4.9 (4/27/2016)
* http: fixed pass errorCode.severity

### 4.4.8 (4/20/2016)
* http: add Map<String, String) queryParams();

### 4.4.7 (4/15/2016 - 4/18/2016)
* template: remove c:msg, use m:text instead
* exception: removed @Warning, use programmable severity (to simplify webservice exception handling and error report)

### 4.4.6 (4/15/2016)
* template: support m:text and m:{attr} for message body and attribute

### 4.4.5 (4/15/2016)
* route: fix dynamic path should not match trailing slash
* error: refactory errorCode support, make webservice client translate

### 4.4.4 (4/14/2016)
* httpclient: put error code to httpclientexception
* rabbitmq: fixed queue listener to report error code on ack
* thread: try to shutdown thread pool gracefully

### 4.4.3 (4/13/2016)
* template: hide templateManager, not needed by application
* mongo: log Bson filter/projection/sort in json string format

### 4.4.2 (4/12/2016)
* json: support Optional as field, queue/ws interface supported too

### 4.4.1 (4/8/2016)
* http: update httpclient request to accept ContentType
* search: update es to 2.3.1
* httpclient: throw HTTPClientException for invalid url

### 4.4.0 (4/7/2016)
* log: put 1M limit of trace log to forward to remote
* log: add message filter support

### 4.3.9 (4/1/2016)
* mongo: support eval for db-migration
* mongo: set connection max idle time to 30mins

### 4.3.8 (3/31/2016)
* hmac: changed input from String to bytes
* search: update es to 2.3.0

### 4.3.7 (3/30/2016)
* validate: add @ValuePattern

### 4.3.6 (3/30/2016)
* http: request supports body() method

### 4.3.5 (3/28/2016)
* http: added 422 status code

### 4.3.4 (3/25/2016)
* log: actionLogContext.put checks duplication to avoid huge trace log

### 4.3.3 (3/23/2016 - 3/25/2016)
* search: update es to 2.2.1
* web: for file response, close exchange when end

### 4.3.2 (3/18/2016)
* mongo: update default timeout to 15s
* rabbitmq: publisher supports message with priority

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
* web: renamed all internal /management/ path to /\_sys/

### 4.1.1 (2/9/2016)
* monitor: initial monitoring draft, forward monitor metrics via logforwarder

### 4.1.0 (2/4/2016)
* template: invalid url attr will write src="", container will write empty if content is null
* search: support 2.2.0, load groovy plugin in test context

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
* search: set ping timeout, support dynamic index name (for alias or time serial index name)

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
* search: ignore cluster name for transport client

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
* search: update ES to 2.1.0
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
* search: API changed to provide more flexibility to operate index and type
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
